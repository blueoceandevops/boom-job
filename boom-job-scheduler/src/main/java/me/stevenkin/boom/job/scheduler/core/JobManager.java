package me.stevenkin.boom.job.scheduler.core;

import lombok.Data;
import me.stevenkin.boom.job.common.dto.JobDetail;
import me.stevenkin.boom.job.common.enums.JobStatus;
import me.stevenkin.boom.job.common.po.App;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobConfig;
import me.stevenkin.boom.job.common.po.JobKey;
import me.stevenkin.boom.job.common.service.JobSchedulerService;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.data.dao.AppInfoDao;
import me.stevenkin.boom.job.data.dao.JobConfigDao;
import me.stevenkin.boom.job.data.dao.JobInfoDao;
import me.stevenkin.boom.job.data.dao.JobScheduleDao;
import me.stevenkin.boom.job.scheduler.SchedulerContext;
import me.stevenkin.boom.job.scheduler.config.BoomJobConfig;
import me.stevenkin.boom.job.scheduler.dubbo.DubboConfigHolder;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class JobManager implements InitializingBean, DisposableBean {
    @Autowired
    private AppInfoDao appInfoDao;
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private JobConfigDao jobConfigDao;
    @Autowired
    private JobScheduleDao jobScheduleDao;
    @Autowired
    private SchedulerContext schedulerContext;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private BoomJobConfig config;
    @Autowired
    private DubboConfigHolder dubboConfigHolder;
    @Autowired
    private JobStatusMachine statusMachine;
    @Autowired
    private JobExecutor jobExecutor;

    private SchedulerFactory schedulers;

    private Map<Long, ScheduledJob> jobCaches = new ConcurrentHashMap<>();

    @Transactional
    public Boolean onlineJob(Long jobId){
        Job job = jobInfoDao.selectById(jobId);
        if (job == null) {
            return Boolean.FALSE;
        }
        if (!statusMachine.isLegalStatus(JobStatus.fromCode(job.getStatus()), JobStatus.ONLINE)) {
            throw new IllegalStateException();
        }
        jobScheduleDao.insertJobScheduleTurnover(jobId, schedulerContext.getSchedulerId(), Instant.now());

        Job job1 = new Job();
        job1.setId(jobId);
        job1.setStatus(JobStatus.ONLINE.getCode());
        job1.setUpdateTime(LocalDateTime.now());
        int count = jobInfoDao.updateWhereStatusIs(job.getStatus(), job1);
        if (count <= 0) {
            throw new IllegalStateException();
        }

        ScheduledJob job2 = new ScheduledJob(jobDetail(job), this);
        jobCaches.put(jobId, job2);
        job2.start();

        return Boolean.TRUE;
    }

    public Boolean triggerJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Job job = jobInfoDao.selectById(jobId);
        if (!statusMachine.isLegalStatus(JobStatus.fromCode(job.getStatus()), JobStatus.RUNNING))
            return Boolean.FALSE;
        return jobCaches.get(jobId).trigger();
    }

    public Boolean pauseJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Job job = jobInfoDao.selectById(jobId);
        if (!statusMachine.isLegalStatus(JobStatus.fromCode(job.getStatus()), JobStatus.PAUSED))
            return Boolean.FALSE;
        Job job1 = new Job();
        job1.setId(jobId);
        job1.setStatus(JobStatus.PAUSED.getCode());
        job1.setUpdateTime(LocalDateTime.now());
        int count = jobInfoDao.updateWhereStatusIs(job.getStatus(), job1);
        if (count <= 0) {
            throw new IllegalStateException();
        }
        return jobCaches.get(jobId).pause();
    }

    public Boolean resumeJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Job job = jobInfoDao.selectById(jobId);
        if (!statusMachine.isLegalStatus(JobStatus.fromCode(job.getStatus()), JobStatus.ONLINE))
            return Boolean.FALSE;
        Job job1 = new Job();
        job1.setId(jobId);
        job1.setStatus(JobStatus.ONLINE.getCode());
        job1.setUpdateTime(LocalDateTime.now());
        int count = jobInfoDao.updateWhereStatusIs(job.getStatus(), job1);
        if (count <= 0) {
            throw new IllegalStateException();
        }
        return jobCaches.get(jobId).resume(jobDetail(job));
    }

    public Boolean offlineJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Job job = jobInfoDao.selectById(jobId);
        if (!statusMachine.isLegalStatus(JobStatus.fromCode(job.getStatus()), JobStatus.OFFLINE))
            return Boolean.FALSE;
        Job job1 = new Job();
        job1.setId(jobId);
        job1.setStatus(JobStatus.OFFLINE.getCode());
        job1.setUpdateTime(LocalDateTime.now());
        int count = jobInfoDao.updateWhereStatusIs(job.getStatus(), job1);
        if (count <= 0) {
            throw new IllegalStateException();
        }
        return jobCaches.get(jobId).offline();
    }

    public Boolean reloadJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Job job = jobInfoDao.selectById(jobId);
        JobStatus jobStatus = JobStatus.fromCode(job.getStatus());
        if (jobStatus != JobStatus.ONLINE && jobStatus != JobStatus.RUNNING) {
            return Boolean.FALSE;
        }
        return jobCaches.get(jobId).reload(jobDetail(job));
    }

    private JobDetail jobDetail(Job job) {
        JobKey jobKey = jobInfoDao.selectJobKeyById(job.getId());
        JobConfig jobConfig = jobConfigDao.selectByJobId(job.getId());
        App app = appInfoDao.selectAppById(job.getAppId());
        Assert.isTrue(jobKey != null, "job key " + job.getId() + " must be exist");
        Assert.isTrue(jobConfig != null, "job config" + job.getId() + " must be exist");
        Assert.isTrue(app != null, "job" + job.getId() + " 'app must be exist");
        return new JobDetail(job, app, jobKey, jobConfig);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            Properties properties = new Properties();
            properties.setProperty("org.quartz.threadPool.threadCount", Integer.toString(config.getQuartz().getScheduleThreadCount()));
            properties.setProperty("org.quartz.threadPool.class", config.getQuartz().getScheduleThreadPoolClass());
            schedulers = new StdSchedulerFactory(properties);
            schedulers.getScheduler().start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        jobCaches.values().forEach(ScheduledJob::shutdown);
        jobCaches.clear();
        schedulers.getScheduler().shutdown();
        zkClient.shutdown();
    }
}
