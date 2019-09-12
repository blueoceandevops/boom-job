package me.stevenkin.boom.job.scheduler.core;

import com.alibaba.dubbo.config.ReferenceConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.enums.JobType;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobKey;
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
    private JobManager jobManager;

    private me.stevenkin.boom.job.common.dto.JobDetail jobDetail;

    private SchedulerFactory schedulers;

    private ZkClient zkClient;

    private JobExecutor jobExecutor;

    private DubboConfigHolder dubboConfigHolder;

    private JobProcessor jobProcessor;

    private ReferenceConfig<JobProcessor> reference;

    public ScheduledJob(me.stevenkin.boom.job.common.dto.JobDetail jobDetail, JobManager jobManager) {
        this.jobDetail = jobDetail;
        this.jobManager = jobManager;
        this.dubboConfigHolder = jobManager.getDubboConfigHolder();
        this.schedulers = jobManager.getSchedulers();
        this.zkClient = jobManager.getZkClient();
        this.jobExecutor  = jobManager.getJobExecutor();
    }

    public Boolean schedule() {
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
        try {
            schedulers.getScheduler().triggerJob(buildJobKey(jobDetail.getJobKey()));
            return Boolean.TRUE;
        } catch (SchedulerException e) {
            log.error("some error happen", e);
        }
        return Boolean.FALSE;
    }

    public Boolean pause() {
        try {
            schedulers.getScheduler().pauseJob(buildJobKey(jobDetail.getJobKey()));
            return Boolean.TRUE;
        } catch (SchedulerException e) {
            log.error("some error happen", e);
        }
        return Boolean.FALSE;
    }

    public Boolean resume(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        if (isDiff(jobDetail)) {
            return reload(jobDetail);
        }
        try {
            schedulers.getScheduler().resumeJob(buildJobKey(jobDetail.getJobKey()));
            return Boolean.TRUE;
        } catch (SchedulerException e) {
            log.error("some error happen", e);
        }
        return Boolean.FALSE;
    }

    public Boolean offline() {
        reference.destroy();
        return delete();
    }

    public Boolean delete() {
        try {
            schedulers.getScheduler().deleteJob(buildJobKey(jobDetail.getJobKey()));
            return Boolean.TRUE;
        } catch (SchedulerException e) {
            log.error("some error happen", e);
        }
        return Boolean.FALSE;
    }

    public Boolean reload(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        this.jobDetail = jobDetail;
        if (!isDiff(jobDetail))
            return Boolean.TRUE;
        return delete() && schedule();
    }

    private org.quartz.JobKey buildJobKey(JobKey jobKey) {
        return org.quartz.JobKey.jobKey(jobKey.getJobClassName(),
                NameKit.getAppId(jobKey.getAppName(), jobKey.getAuthor()));
    }

    private JobDataMap buildJobData(me.stevenkin.boom.job.common.dto.JobDetail jobDetail, JobProcessor jobProcessor, JobExecutor jobExecutor) {
        JobDataMap jobData = new JobDataMap();
        jobData.put("jobExecutor", jobExecutor);
        jobData.put("jobDetail", jobDetail);
        jobData.put("jobProcessor", jobProcessor);
        return jobData;
    }

    private Trigger buildTrigger(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        Job job = jobDetail.getJob();
        TriggerBuilder<Trigger> triggerBuilder = newTrigger();

        triggerBuilder.withIdentity(buildTriggerKey(jobDetail.getJobKey()));
        CronScheduleBuilder cronScheduleBuilder = cronSchedule(jobDetail.getJobConfig().getCron());
        //TODO misfire
        triggerBuilder.withSchedule(cronScheduleBuilder);

        return triggerBuilder.build();
    }

    private void buildJobProcessor(JobKey jobKey) {
        reference = new ReferenceConfig<>();
        reference.setApplication(dubboConfigHolder.getApplicationConfig());
        reference.setRegistry(dubboConfigHolder.getRegistryConfig());
        reference.setInterface(JobProcessor.class);
        reference.setGroup(NameKit.getJobId(jobKey.getAppName(), jobKey.getAuthor(), jobKey.getJobClassName()));
        jobProcessor = reference.get();
    }

    private TriggerKey buildTriggerKey(JobKey jobKey) {
        return TriggerKey.triggerKey(jobKey.getJobClassName(),
                NameKit.getAppId(jobKey.getAppName(), jobKey.getAuthor()));
    }

    private boolean isDiff(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        //TODO diff
        return false;
    }

    @Override
    public void start() {
        buildJobProcessor(jobDetail.getJobKey());
        schedule();
    }

    @Override
    public void shutdown() {
        if (reference != null) {
            reference.destroy();
        }
        jobManager.offlineJob(jobDetail.getJob().getId());
    }
}
