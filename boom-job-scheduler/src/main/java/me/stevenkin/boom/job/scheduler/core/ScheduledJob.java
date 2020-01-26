package me.stevenkin.boom.job.scheduler.core;

import com.alibaba.dubbo.config.ReferenceConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.exception.ScheduleException;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobKey;
import me.stevenkin.boom.job.common.service.ClientProcessor;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.scheduler.dubbo.DubboConfigHolder;
import org.quartz.*;


import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Data
@NoArgsConstructor
@Slf4j
public class ScheduledJob {
    private JobManager jobManager;

    private me.stevenkin.boom.job.common.dto.JobDetail jobDetail;

    private SchedulerFactory schedulers;

    private ZkClient zkClient;

    private JobExecutor jobExecutor;

    private DubboConfigHolder dubboConfigHolder;

    private ClientProcessor clientProcessor;

    private volatile boolean isPaused; //job is not exist in schedule pool

    public ScheduledJob(me.stevenkin.boom.job.common.dto.JobDetail jobDetail, JobManager jobManager) {
        this.jobDetail = jobDetail;
        this.jobManager = jobManager;
        this.dubboConfigHolder = jobManager.getDubboConfigHolder();
        this.schedulers = jobManager.getSchedulers();
        this.zkClient = jobManager.getZkClient();
        this.jobExecutor  = jobManager.getJobExecutor();
        buildJobProcessor(jobDetail.getJobKey());
        this.isPaused = true;
    }

    public Boolean schedule() {
        org.quartz.JobKey jobKey = buildJobKey(jobDetail.getJobKey());

        Scheduler scheduler = null;
        try {
            scheduler = schedulers.getScheduler();
        } catch (SchedulerException e) {
            log.error("some error happen", e);
            throw new ScheduleException(e);
        }

        JobDataMap jobData = buildJobData(jobDetail, clientProcessor, jobExecutor);

        org.quartz.JobDetail quartzJob = newJob(ProxyJob.class)
                .withIdentity(jobKey)
                .usingJobData(jobData)
                .build();

        Trigger trigger = buildTrigger(jobDetail);

        try {
            scheduler.scheduleJob(quartzJob, trigger);
        } catch (SchedulerException e) {
            log.error("some error happen", e);
            throw new ScheduleException(e);
        }
        isPaused = false;
        return Boolean.TRUE;
    }

    public Boolean trigger() {
        if (isPaused)
            return Boolean.FALSE;
        jobExecutor.execute(jobDetail, clientProcessor);
        return Boolean.TRUE;
    }

    public Boolean pause() {
        return delete();

    }

    public Boolean resume(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        this.jobDetail = jobDetail;
        return schedule();
    }

    public Boolean offline() {
        return delete();
    }

    public Boolean delete() {
        try {
            Scheduler scheduler = schedulers.getScheduler();
            TriggerKey triggerKey = buildTriggerKey(jobDetail.getJobKey());
            org.quartz.JobKey jobKey = buildJobKey(jobDetail.getJobKey());
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
            isPaused = true;
            return Boolean.TRUE;
        } catch (SchedulerException e) {
            log.error("some error happen", e);
            throw new ScheduleException(e);
        }
    }

    public Boolean reload(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        if (!delete()) {
            return Boolean.FALSE;
        }
        this.jobDetail = jobDetail;
        return schedule();
    }

    private org.quartz.JobKey buildJobKey(JobKey jobKey) {
        return org.quartz.JobKey.jobKey(jobKey.getJobClassName(),
                NameKit.getAppKey(jobKey.getAppName(), jobKey.getAuthor()));
    }

    private JobDataMap buildJobData(me.stevenkin.boom.job.common.dto.JobDetail jobDetail, ClientProcessor clientProcessor, JobExecutor jobExecutor) {
        JobDataMap jobData = new JobDataMap();
        jobData.put("jobExecutor", jobExecutor);
        jobData.put("jobDetail", jobDetail);
        jobData.put("clientProcessor", clientProcessor);
        return jobData;
    }

    private Trigger buildTrigger(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        TriggerBuilder<Trigger> triggerBuilder = newTrigger();

        triggerBuilder.withIdentity(buildTriggerKey(jobDetail.getJobKey()));
        CronScheduleBuilder cronScheduleBuilder = cronSchedule(jobDetail.getJobConfig().getCron());
        //TODO misfire
        triggerBuilder.withSchedule(cronScheduleBuilder);

        return triggerBuilder.build();
    }

    private void buildJobProcessor(JobKey jobKey) {
        String group = NameKit.getAppKey(jobKey.getAppName(), jobKey.getAuthor());
        clientProcessor = jobManager.getReferenceCache().get(group);
        if (clientProcessor == null) {
            synchronized (jobManager.getReferenceCache()) {
                clientProcessor = jobManager.getReferenceCache().get(group);
                if (clientProcessor == null) {
                    ReferenceConfig<ClientProcessor> reference = new ReferenceConfig<>();
                    reference.setApplication(dubboConfigHolder.getApplicationConfig());
                    reference.setRegistry(dubboConfigHolder.getRegistryConfig());
                    reference.setInterface(ClientProcessor.class);
                    reference.setCheck(false);
                    reference.setGroup(group);
                    clientProcessor = reference.get();
                    jobManager.getReferenceCache().put(group, clientProcessor);
                }
            }
        }
    }

    private TriggerKey buildTriggerKey(JobKey jobKey) {
        return TriggerKey.triggerKey(jobKey.getJobClassName(),
                NameKit.getAppKey(jobKey.getAppName(), jobKey.getAuthor()));
    }

    private boolean isDiff(me.stevenkin.boom.job.common.dto.JobDetail jobDetail) {
        return !jobDetail.getJobConfig().getCron().equals(this.jobDetail.getJobConfig().getCron());
    }
}
