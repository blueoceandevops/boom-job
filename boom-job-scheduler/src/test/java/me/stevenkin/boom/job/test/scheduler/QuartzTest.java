package me.stevenkin.boom.job.test.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class QuartzTest {
    private Scheduler scheduler;

    @Before
    public void before() {
        try {
            Properties properties = new Properties();
            properties.setProperty("org.quartz.threadPool.threadCount", "4");
            SchedulerFactory schedulers = new StdSchedulerFactory(properties);
            schedulers.getScheduler().start();
            scheduler = schedulers.getScheduler();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test() {
        String jobName = "job1";
        String triggerName = "trigger1";
        String groupName = "group1";

        try {
            JobDetail job = JobBuilder.newJob(TestJob.class).withIdentity("job1", "group1").build();
            CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1")
                    .withSchedule(cronSchedule("0/2 * * * * ?")).build();
            scheduler.scheduleJob(job, trigger);

            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, groupName);
            //下面三个组件都需删除
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(JobKey.jobKey(jobName, groupName));// 删除任务

            scheduler.triggerJob(JobKey.jobKey(jobName, groupName));
        }catch (Exception e) {
            log.error("error ", e);
        }
    }
}
