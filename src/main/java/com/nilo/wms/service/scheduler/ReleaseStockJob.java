package com.nilo.wms.service.scheduler;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.enums.MethodEnum;
import com.nilo.wms.common.util.DateUtil;
import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.service.platform.RedisUtil;
import com.nilo.wms.service.platform.SpringContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 释放库存锁定任务
 * Created by Administrator on 2018/7/30.
 */
public class ReleaseStockJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static AtomicBoolean RUN = new AtomicBoolean(false);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (RUN.compareAndSet(false, true)) {

                initSession();

                String clientCode = "kilimall";
                String nowDate = DateUtil.formatCurrent(DateUtil.LONG_WEB_FORMAT);
                Set<String> lockList = RedisUtil.keys(RedisUtil.getLockOrderKey(clientCode, "*"));
                BasicDataService basicDataService = SpringContext.getBean(BasicDataService.class);
                for (String l : lockList) {
                    String lockTime = RedisUtil.hget(l, RedisUtil.LOCK_TIME);
                    if (getDateSpace(lockTime, nowDate) >= 2) {
                        String orderNo = l.replace(RedisUtil.getLockOrderKey(clientCode, ""), "");
                        basicDataService.unLockStorage(orderNo);
                    }
                }
            } else {
                logger.info("Already Execute EmailWarningJob.........");
            }
        } catch (Exception ex) {
            logger.error("EmailWarningJob failed. {}", ex.getMessage(), ex);
        } finally {
            RUN.set(false);
        }

    }

    private void initSession() {
        Principal principal = new Principal();
        principal.setClientCode("kilimall");
        principal.setMethod(MethodEnum.UN_LOCK_STORAGE.getCode());
        principal.setCustomerId("KILIMALL");
        principal.setWarehouseId("KE01");
        SessionLocal.setPrincipal(principal);
    }

    public static int getDateSpace(String date1, String date2) throws ParseException {
        Calendar calst = Calendar.getInstance();
        Calendar caled = Calendar.getInstance();
        calst.setTime(parseDate(date1));
        caled.setTime(parseDate(date2));
        //设置时间为0时
        calst.set(Calendar.HOUR_OF_DAY, 0);
        calst.set(Calendar.MINUTE, 0);
        calst.set(Calendar.SECOND, 0);
        caled.set(Calendar.HOUR_OF_DAY, 0);
        caled.set(Calendar.MINUTE, 0);
        caled.set(Calendar.SECOND, 0);
        //得到两个日期相差的天数
        int days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst.getTime().getTime() / 1000)) / 3600 / 24;
        return days;
    }

    public static Date parseDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.LONG_WEB_FORMAT);
        Date currentTime_2 = formatter.parse(date);
        return currentTime_2;
    }
}
