package me.stevenkin.boom.job.scheduler.core;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.JobDetail;
import me.stevenkin.boom.job.common.enums.JobStatus;
import me.stevenkin.boom.job.common.enums.JobTriggerType;
import me.stevenkin.boom.job.common.exception.ScheduleException;
import me.stevenkin.boom.job.common.po.App;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobConfig;
import me.stevenkin.boom.job.common.po.JobKey;
import me.stevenkin.boom.job.common.service.ClientProcessor;
import me.stevenkin.boom.job.common.support.Attachment;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.scheduler.service.FailoverService;
import me.stevenkin.boom.job.storage.dao.AppInfoDao;
import me.stevenkin.boom.job.storage.dao.JobConfigDao;
import me.stevenkin.boom.job.storage.dao.JobInfoDao;
import me.stevenkin.boom.job.storage.dao.JobScheduleDao;
import me.stevenkin.boom.job.scheduler.config.BoomJobConfig;
import me.stevenkin.boom.job.scheduler.dubbo.DubboConfigHolder;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Component
@Data
@Slf4j
public class JobManager extends Lifecycle {
    @Autowired
    private AppInfoDao appInfoDao;
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private JobConfigDao jobConfigDao;
    @Autowired
    private JobScheduleDao jobScheduleDao;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private BoomJobConfig config;
    @Autowired
    private DubboConfigHolder dubboConfigHolder;
    @Autowired
    private JobExecutor jobExecutor;
    @Autowired
    private FailoverService failoverService;
    @Setter
    private String schedulerId;
    @Getter
    private CountDownLatch latch = new CountDownLatch(1);

    private SchedulerFactory schedulers;

    private Map<Long, ScheduledJob> jobCaches = new ConcurrentHashMap<>();
    @Setter
    private Map<String, ClientProcessor> referenceCache = new ConcurrentHashMap<>();

    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean onlineJob(Long jobId){
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        return doOnlineJob(jobId);
    }

    private Boolean doOnlineJob(Long jobId) {
        if (jobCaches.containsKey(jobId)) {
            return Boolean.TRUE;
        }
        Integer n = jobScheduleDao.onlineJob(jobId, schedulerId);
        if (n < 1) {
            return Boolean.FALSE;
        }

        ScheduledJob scheduledJob = new ScheduledJob(jobDetail(jobId), this);
        scheduledJob.schedule();
        jobCaches.put(jobId, scheduledJob);

        return Boolean.TRUE;
    }

    public synchronized Boolean triggerJob(Long jobId){
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        return doTriggerJob(jobId, JobTriggerType.MANUAL, new Attachment());
    }

    private Boolean doTriggerJob(Long jobId, JobTriggerType type, Attachment attach) {
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        return jobCaches.get(jobId).trigger(type, attach);
    }

    public synchronized Boolean planTriggerJob(Long jobId, Long planJobInstanceId) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        if (!doOnlineJob(jobId))
            return Boolean.FALSE;
        return doTriggerJob(jobId, JobTriggerType.PLAN, new Attachment().put("planJobInstanceId", planJobInstanceId));
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean pauseJob(Long jobId){
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Integer n = jobScheduleDao.pauseJob(jobId, schedulerId);
        if (n < 1) {
            return Boolean.FALSE;
        }
        return jobCaches.get(jobId).pause();
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean resumeJob(Long jobId){
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Integer n = jobScheduleDao.resumeJob(jobId, schedulerId);
        if (n < 1) {
            return Boolean.FALSE;
        }
        return jobCaches.get(jobId).resume(jobDetail(jobId));
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean offlineJob(Long jobId){
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Integer n = jobScheduleDao.offlineJob(jobId, schedulerId);
        if (n < 1) {
            return Boolean.FALSE;
        }
        ScheduledJob scheduledJob = jobCaches.get(jobId);
        boolean b = scheduledJob.offline();
        jobCaches.remove(jobId);
        return b;
    }

    public synchronized Boolean reloadJob(Long jobId){
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;

        return jobCaches.get(jobId).reload(jobDetail(jobId));
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean failoverJob(Long jobId, String schedulerId) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        if (schedulerId.equals(this.schedulerId))
            return Boolean.FALSE;
        Integer n = jobScheduleDao.failoverJob(jobId, schedulerId, this.schedulerId);
        if (n < 1) {
            return Boolean.FALSE;
        }
        Job job = jobInfoDao.selectById(jobId);
        Assert.isTrue(job != null, "job " + jobId + " must be exist");
        JobStatus jobStatus = JobStatus.fromCode(job.getStatus());
        ScheduledJob scheduledJob;
        switch (jobStatus) {
            case ONLINE:
                scheduledJob = new ScheduledJob(jobDetail(jobId), this);
                scheduledJob.schedule();
                jobCaches.put(jobId, scheduledJob);
                break;
            case PAUSED:
                scheduledJob = new ScheduledJob(jobDetail(jobId), this);
                jobCaches.put(jobId, scheduledJob);
                break;
            default:
                throw new ScheduleException();
        }
        return Boolean.TRUE;
    }

    private JobDetail jobDetail(Long jobId) {
        Job job = jobInfoDao.selectById(jobId);
        JobKey jobKey = jobInfoDao.selectJobKeyById(jobId);
        JobConfig jobConfig = jobConfigDao.selectByJobId(jobId);
        App app = appInfoDao.selectAppById(jobId);
        Assert.isTrue(job != null, "job " + jobId + " must be exist");
        Assert.isTrue(jobKey != null, "job key " + jobId + " must be exist");
        Assert.isTrue(jobConfig != null, "job config" + jobId + " must be exist");
        Assert.isTrue(app != null, "job app" + jobId + " must be exist");
        return new JobDetail(job, app, jobKey, jobConfig);
    }

    @Override
    public void doStart() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.threadPool.threadCount", Integer.toString(config.getQuartz().getScheduleThreadCount()));
        properties.setProperty("org.quartz.threadPool.class", config.getQuartz().getScheduleThreadPoolClass());
        schedulers = new StdSchedulerFactory(properties);
        schedulers.getScheduler().start();
    }

    @Override
    public void doPause() throws Exception {
        jobCaches.values().forEach(ScheduledJob::delete);
        jobCaches.clear();
        schedulers.getScheduler().shutdown();
        failoverService.processSchedulerFailed(schedulerId);
    }

    @Override
    public void doResume() throws Exception {
        doStart();
    }

    @Override
    public void doShutdown() throws Exception {

    }
}
