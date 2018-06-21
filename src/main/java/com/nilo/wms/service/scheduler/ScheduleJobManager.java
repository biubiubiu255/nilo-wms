package com.nilo.wms.service.scheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by admin on 2017/11/29.
 */
public class ScheduleJobManager {

    public static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    private static String JOB_GROUP_NAME = "DEFAULT_JOB_GROUP_NAME";
    private static String TRIGGER_GROUP_NAME = "DEFAULT_TRIGGER_GROUP_NAME";

    public static void addJob(String jobName, Class cls, String cron)
            throws SchedulerException {
        addJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, cls, cron);
    }


    public static void addJob(String jobName, String jobGroupName,
                              String triggerName, String triggerGroupName, Class cls, String cron)
            throws SchedulerException {


        Scheduler scheduler = schedulerFactory.getScheduler();
        JobDetail jobDetail = JobBuilder.newJob(cls)
                .withIdentity(jobName, jobGroupName).build();
        Trigger trigger = TriggerBuilder.newTrigger()// 创建一个新的TriggerBuilder来规范一个触发器
                .withIdentity(triggerName, triggerGroupName)// 给触发器起一个名字和组名
                .startNow()// 立即执行
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)) // 触发器的执行时间
                .build();// 产生触发器
        scheduler.scheduleJob(jobDetail, trigger);
        // 启动
        if (!scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    /**
     * @param jobName
     * @param cron
     * @throws SchedulerException
     * @Description: 修改一个任务的触发时间(使用默认的任务组名，触发器名，触发器组名)
     */
    public static void modifyJobTime(String jobName, String cron) throws SchedulerException {

        Scheduler scheduler = schedulerFactory.getScheduler();

        TriggerKey triggerKey = new TriggerKey(jobName, TRIGGER_GROUP_NAME);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger == null) {
            return;
        }
        String oldTime = trigger.getCronExpression();
        if (!oldTime.equalsIgnoreCase(cron)) {
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobName,
                    JOB_GROUP_NAME));
            Class objJobClass = jobDetail.getJobClass();
            removeJob(jobName);
            addJob(jobName, objJobClass, cron);
        }
        // 启动
        if (!scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    /**
     * @param jobName
     * @throws SchedulerException
     * @Description: 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
     */
    public static void removeJob(String jobName) throws SchedulerException {
        removeJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME);
    }

    /**
     * 移除任务
     *
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     * @throws SchedulerException
     */
    public static void removeJob(String jobName, String jobGroupName,
                                 String triggerName, String triggerGroupName)
            throws SchedulerException {

        Scheduler scheduler = schedulerFactory.getScheduler();

        JobKey jobKey = new JobKey(jobName, jobGroupName);
        // 停止触发器
        scheduler.pauseJob(jobKey);
        scheduler.unscheduleJob(new TriggerKey(triggerName, triggerGroupName));// 移除触发器
        scheduler.deleteJob(jobKey);// 删除任务
    }

    /**
     * 启动所有任务
     *
     * @throws SchedulerException
     */
    public static void startJobs() throws SchedulerException {

        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduler.start();
    }

    /**
     * 关闭所有定时任务
     *
     * @throws SchedulerException
     */
    public static void shutdownJobs() throws SchedulerException {
        Scheduler scheduler = schedulerFactory.getScheduler();
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
