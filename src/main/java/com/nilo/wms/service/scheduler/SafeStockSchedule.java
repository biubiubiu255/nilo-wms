/**
 * KILIMALL.com Inc.
 * Copyright (c) 2015-2016 All Rights Reserved.
 */
package com.nilo.wms.service.scheduler;

import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.dao.flux.FluxOutboundDao;
import com.nilo.wms.dao.flux.FluxInventoryDao;
import com.nilo.wms.dto.common.ClientConfig;
import com.nilo.wms.dto.flux.FluxOutboundDetails;
import com.nilo.wms.service.config.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * 安全库存任务
 */
public class SafeStockSchedule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{configProperties['min_month_sale']}")
    private int min_month_sale;
    @Value("#{configProperties['replenishment_lead_time']}")
    private int replenishment_lead_time;
    @Value("#{configProperties['customer_factor']}")
    private double customer_factor;
    @Autowired
    private FluxOutboundDao fluxOutboundDao;
    @Autowired
    private FluxInventoryDao skuDao;

    public void execute() {
        try {
            String clientCode = "kilimall";
            ClientConfig config = SystemConfig.getClientConfig().get(clientCode);
            if (config == null) {
                throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
            }

            //查询上个月销售数据
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.DATE, 1);
            String startDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

            Calendar calendar1 = Calendar.getInstance();
            calendar1.add(Calendar.MONTH, -1);
            calendar1.set(Calendar.DATE, calendar1.getActualMaximum(Calendar.DATE));
            String endDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar1.getTime());
            //总天数
            int days = calendar1.getActualMaximum(Calendar.DATE);
            //查询销售量
            List<FluxOutboundDetails> list = fluxOutboundDao.querySales(config.getCustomerCode(), startDate, endDate);
            Iterator<FluxOutboundDetails> iterator = list.iterator();
            while (iterator.hasNext()) {
                FluxOutboundDetails details = iterator.next();
                //移除掉销售量过小的商品
                if (details.getQty() < min_month_sale) {
                    iterator.remove();
                }
            }

            if (list.size() == 0) return;
            for (FluxOutboundDetails d : list) {
                //计算安全库存
                long safeQty = Math.round(customer_factor * Math.sqrt(1 * 1 * (d.getQty() / days) + 1 * 1 * replenishment_lead_time * replenishment_lead_time));
                skuDao.updateSafeQty(config.getCustomerCode(), d.getSku(), "" + safeQty);
            }
        } catch (Exception ex) {
            logger.error("SyncStockSchedule failed. {}", ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        long safeQty = Math.round(1.65 * Math.sqrt(1 * 1 * (168 / 31) + 1 * 1 * 6 * 6));
        System.out.println(safeQty);
    }
}
