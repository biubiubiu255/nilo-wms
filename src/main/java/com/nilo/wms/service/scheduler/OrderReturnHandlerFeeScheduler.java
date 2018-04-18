package com.nilo.wms.service.scheduler;

import com.nilo.wms.common.enums.MoneyType;
import com.nilo.wms.dto.Fee;
import com.nilo.wms.service.FeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * 入仓费用
 * Created by Administrator on 2017/6/9.
 */
public class OrderReturnHandlerFeeScheduler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FeeService feeService;

    public void execute() {
        try {
            logger.info("====start order return ====");
            Calendar calendar = Calendar.getInstance();
            calendar.add(calendar.DATE, -1);//把日期往后增加一天.整数往后推,负数往前移动
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            //查询入库
            List<Fee> list = feeService.queryOrderReturnHandlerFee("kilimall",dateString);
            feeService.syncToNOS(list, "kilimall",dateString, MoneyType.Order_Return.getCode());
            logger.info("====end order return ====");
        } catch (Exception ex) {
            logger.error("get delivery fee faild." , ex.getMessage(),ex);
        }
    }


}
