package me.stevenkin.boom.job.scheduler.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.enums.JobType;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.model.Job;
import me.stevenkin.boom.job.common.model.JobKey;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.scheduler.dubbo.DubboConfigHolder;
import org.quartz.*;

import java.time.ZoneId;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Data
@NoArgsConstructor
@Slf4j
public class ScheduledJob implements Lifecycle {
    private me.stevenkin.boom.job.common.bean.JobDetail jobDetail;

    private SchedulerFactory schedulers;

    private ZkClient zkClient;

    private CommandProcessor commandProcessor;

    private JobExecutor jobExecutor;

    private DubboConfigHolder dubboConfigHolder;

    private JobProcessor jobProcessor;

    public ScheduledJob(me.stevenkin.boom.job.common.bean.JobDetail jobDetail, DubboConfigHolder dubboConfigHolder, SchedulerFactory schedulers, ZkClient zkClient, JobExecutor jobExecutor) {
        this.jobDetail = jobDetail;
        this.dubboConfigHolder = dubboConfigHolder;
        this.schedulers = schedulers;
        this.zkClient = zkClient;
        this.jobExecutor  = jobExecutor;
        this.commandProcessor = new CommandProcessor(jobDetail.getJobKey(), zkClient, this);
    }

    public Boolean online() {
        org.quartz.JobKey jobKey = buildJobKey(jobDetail.getJobKey());

        Scheduler scheduler = null;
        try {
            scheduler = schedulers.getScheduler();
        } catch (SchedulerException e) {
            log.error("some error happen", e);
            throw new RuntimeException(e);
        }

        JobDataMap jobData = buildJobData(jobDetail, jobProcessor, jobExecutor);

        org.quartz.JobDetail quartzJob = newJob(ProxyJob.class)
                .withIdentity(jobKey)
                .usingJobData(jobData)
                .build();


        Trigger trigger = buildTrigger(jobDetail);

        try {
            scheduler.scheduleJob(quartzJob, trigger);
        } catch (SchedulerException e) {
            log.error("some error happen", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean trigger() {
        return null;
    }

    public Boolean pause() {
        return null;
    }

    public Boolean resume() {
        return null;
    }

    public Boolean offline() {
        return null;
    }

    public Boolean reload(Job job) {
        return null;
    }

    private org.quartz.JobKey buildJobKey(JobKey jobKey) {
        return org.quartz.JobKey.jobKey(jobKey.getJobClassName(),
                NameKit.getAppId(jobKey.getAppName(), jobKey.getAuthor(), jobKey.getVersion()));
    }

    private JobDataMap buildJobData(me.stevenkin.boom.job.common.bean.JobDetail jobDetail, JobProcessor jobProcessor, JobExecutor jobExecutor) {
        JobDataMap jobData = new JobDataMap();
        jobData.put("jobExecutor", jobExecutor);
        jobData.put("jobDetail", jobDetail);
        jobData.put("jobProcessor", jobProcessor);
        return jobData;
    }

    private Trigger buildTrigger(me.stevenkin.boom.job.common.bean.JobDetail jobDetail) {
        Job job = jobDetail.getJob();
        JobType jobType = JobType.fromCode(job.getType());
        TriggerBuilder<Trigger> triggerBuilder = newTrigger();

        if (job.getStartTime() != null) {
            triggerBuilder.startAt(Date.from(job.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (job.getEndTime() != null) {
            triggerBuilder.endAt(Date.from(job.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        }

        triggerBuilder.withIdentity(buildTriggerKey(jobDetail.getJobKey()));
        switch (jobType) {
            case SIMPLE:
                SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule();
                simpleScheduleBuilder.withIntervalInSeconds(job.getRepeatInterval());
                if (job.isRepeatForever()) {
                    simpleScheduleBuilder.repeatForever();
                }else {
                    simpleScheduleBuilder.withRepeatCount(job.getRepeatCount());
                }
                //TODO misfire
                triggerBuilder.withSchedule(simpleScheduleBuilder);
                break;
            case CRON:
                CronScheduleBuilder cronScheduleBuilder = cronSchedule(job.getCron());
                //TODO misfire
                triggerBuilder.withSchedule(cronScheduleBuilder);
                break;
            default:
                throw new IllegalStateException();
        }

        return triggerBuilder.build();
    }

    private TriggerKey buildTriggerKey(JobKey jobKey) {
        return TriggerKey.triggerKey(jobKey.getJobClassName(),
                NameKit.getAppId(jobKey.getAppName(), jobKey.getAuthor(), jobKey.getVersion()));
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
