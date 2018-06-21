package com.nilo.wms.service.scheduler;

import com.nilo.wms.common.enums.MoneyType;
import com.nilo.wms.dto.fee.Fee;
import com.nilo.wms.service.FeeService;
import com.nilo.wms.service.platform.SpringContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 订单操作费
 * Created by Administrator on 2017/6/9.
 */
public class OrderHandleFeeJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static AtomicBoolean RUN = new AtomicBoolean(false);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            if (RUN.compareAndSet(false, true)) {
                logger.info("====start order handler fee====");
                Calendar calendar = Calendar.getInstance();
                calendar.add(calendar.DATE, -1);//把日期往后增加一天.整数往后推,负数往前移动
                String dateString = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                //查询入库
                FeeService feeService = SpringContext.getBean(FeeService.class);
                List<Fee> list = feeService.queryOrderHandlerFee("kilimall", dateString);
                //按店铺推送
                Map<String, List<Fee>> map = new HashMap<>();
                for (Fee f : list) {
                    if (map.containsKey(f.getStore_id())) {
                        List<Fee> storeFee = map.get(f.getStore_id());
                        storeFee.add(f);
                    } else {
                        List<Fee> storeFee = new ArrayList<>();
                        storeFee.add(f);
                        map.put(f.getStore_id(), storeFee);
                    }
                }

                for (Map.Entry<String, List<Fee>> entry : map.entrySet()) {
                    feeService.syncToNOS(entry.getValue(), "kilimall", dateString, MoneyType.Order_Handler.getCode());
                }

                logger.info("====end order handler fee====");
            } else {
                logger.info("Already Execute OrderHandleFeeJob.........");
            }
        } catch (Exception ex) {
            logger.error("OrderHandleFeeJob failed. {}", ex.getMessage(), ex);
        } finally {
            RUN.set(false);
        }
    }
}
