package com.nilo.wms.service.impl;

import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.exception.SysErrorCode;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.dao.platform.ScheduleJobDao;
import com.nilo.wms.dto.ScheduleJob;
import com.nilo.wms.service.ScheduleService;
import com.nilo.wms.service.scheduler.ScheduleJobManager;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ronny.zeng
 * @date 2018/6/21.
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleJobDao scheduleJobDao;

    @Override
    public List<ScheduleJob> listAll() {
        return scheduleJobDao.listAll();
    }

    @Override
    public void start(String jobName) {

        ScheduleJob job = scheduleJobDao.getByJobName(jobName);
        try {
            ScheduleJobManager.addJob(job.getJobName(), Class.forName(job.getClassName()), job.getCornExpression());
            ScheduleJob update = new ScheduleJob();
            update.setJobName(jobName);
            update.setStatus(1);
            scheduleJobDao.update(update);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void stop(String jobName) {
        try {
            ScheduleJobManager.removeJob(jobName);
            ScheduleJob job = new ScheduleJob();
            job.setJobName(jobName);
            job.setStatus(0);
            scheduleJobDao.update(job);
        } catch (SchedulerException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void modifyCorn(String jobName, String corn) {
        try {



            ScheduleJobManager.modifyJobTime(jobName, corn);

            ScheduleJob job = new ScheduleJob();
            job.setJobName(jobName);
            job.setCornExpression(corn);
            scheduleJobDao.update(job);

        } catch (SchedulerException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

}
