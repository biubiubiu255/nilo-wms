package com.nilo.wms.service.scheduler;

import com.nilo.wms.dto.ScheduleJob;
import com.nilo.wms.service.ScheduleService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by admin on 2017/11/30.
 */
@Component
public class ScheduleInit implements InitializingBean {

    @Autowired
    private ScheduleService scheduleService;

    @Override
    public void afterPropertiesSet() throws Exception {

        //查询所有定时任务列表
        List<ScheduleJob> list = scheduleService.listAll();
        if (list == null || list.size() == 0) {
            return;
        }
        //添加任务
        for (ScheduleJob scheduleJob : list) {
            if (scheduleJob.getStatus() == 1) {
                Class cls = Class.forName(scheduleJob.getClassName());
                ScheduleJobManager.addJob(scheduleJob.getJobName(), cls, scheduleJob.getCornExpression());
            }
        }
    }
}
