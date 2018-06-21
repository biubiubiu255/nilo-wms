/**
 * KILIMALL.com Inc.
 * Copyright (c) 2015-2016 All Rights Reserved.
 */
package com.nilo.wms.service.scheduler;

import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.service.platform.SpringContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 同步库存任务
 */
public class SyncStockJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static AtomicBoolean RUN = new AtomicBoolean(false);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            if (RUN.compareAndSet(false, true)) {
                logger.info("====start SyncStockSchedule ====");
                String clientCode = "kilimall";

                BasicDataService basicDataService = SpringContext.getBean(BasicDataService.class);

                basicDataService.syncStock(clientCode);
                logger.info(" ======= end SyncStockSchedule =======");
            } else {
                logger.info("Already Execute SyncStockJob.........");
            }
        } catch (Exception ex) {
            logger.error("SyncStockJob failed. {}", ex.getMessage(), ex);
        } finally {
            RUN.set(false);
        }
    }
}
