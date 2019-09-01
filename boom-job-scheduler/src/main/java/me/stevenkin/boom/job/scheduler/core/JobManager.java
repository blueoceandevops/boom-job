package me.stevenkin.boom.job.scheduler.core;

import me.stevenkin.boom.job.common.bean.JobDetail;
import me.stevenkin.boom.job.common.enums.JobStatus;
import me.stevenkin.boom.job.common.model.App;
import me.stevenkin.boom.job.common.model.Job;
import me.stevenkin.boom.job.common.model.JobConfig;
import me.stevenkin.boom.job.common.model.JobKey;
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
public class JobManager implements InitializingBean , DisposableBean {
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

        JobKey jobKey = jobInfoDao.selectJobKeyById(jobId);
        JobConfig jobConfig = jobConfigDao.selectByJobId(jobId);
        App app = appInfoDao.selectAppById(job.getAppId());
        Assert.isTrue(jobKey != null, "job key " + jobId + " must be exist");
        Assert.isTrue(jobConfig != null, "job config" + jobId + " must be exist");
        Assert.isTrue(app != null, "job" + jobId + " 'app must be exist");
        ScheduledJob job2 = new ScheduledJob(new JobDetail(job, app, jobKey, jobConfig), dubboConfigHolder, schedulers, zkClient, jobExecutor);
        jobCaches.put(jobId, job2);
        job2.start();
        return Boolean.TRUE;
    }

    public Boolean triggerJob(Long jobId){
        return null;
    }

    public Boolean pauseJob(Long jobId){
        return null;
    }

    public Boolean resumeJob(Long jobId){
        return null;
    }

    public Boolean offlineJob(Long jobId){
        return null;
    }

    public Boolean reloadJob(Long jobId){
        return null;
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
        schedulers.getScheduler().shutdown();
        zkClient.shutdown();
    }
}
