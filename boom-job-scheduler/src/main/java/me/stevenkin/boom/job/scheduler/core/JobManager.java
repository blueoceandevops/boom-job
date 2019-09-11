package me.stevenkin.boom.job.scheduler.core;

import lombok.Data;
import me.stevenkin.boom.job.common.dto.JobDetail;
import me.stevenkin.boom.job.common.po.App;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobConfig;
import me.stevenkin.boom.job.common.po.JobKey;
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
import org.springframework.util.Assert;

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

    public synchronized Boolean onlineJob(Long jobId){
        Integer n = jobScheduleDao.lockJob(jobId, schedulerContext.getSchedulerId());
        if (n < 1) {
            return Boolean.FALSE;
        }

        ScheduledJob scheduledJob = new ScheduledJob(jobDetail(jobId), this);
        jobCaches.put(jobId, scheduledJob);
        scheduledJob.start();

        return Boolean.TRUE;
    }

    public synchronized Boolean triggerJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        return jobCaches.get(jobId).trigger();
    }

    public synchronized Boolean pauseJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Integer n = jobScheduleDao.pauseJob(jobId);
        if (n < 1) {
            return Boolean.FALSE;
        }
        return jobCaches.get(jobId).pause();
    }

    public synchronized Boolean resumeJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Integer n = jobScheduleDao.resumeJob(jobId);
        if (n < 1) {
            return Boolean.FALSE;
        }
        return jobCaches.get(jobId).resume(jobDetail(jobId));
    }

    public synchronized Boolean offlineJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;
        Integer n = jobScheduleDao.offlineJob(jobId);
        if (n < 1) {
            return Boolean.FALSE;
        }
        return jobCaches.get(jobId).offline();
    }

    public synchronized Boolean reloadJob(Long jobId){
        if (!jobCaches.containsKey(jobId))
            return Boolean.FALSE;

        return jobCaches.get(jobId).reload(jobDetail(jobId));
    }

    private JobDetail jobDetail(Long jobId) {
        Job job = jobInfoDao.selectById(jobId);
        JobKey jobKey = jobInfoDao.selectJobKeyById(jobId);
        JobConfig jobConfig = jobConfigDao.selectByJobId(jobId);
        App app = appInfoDao.selectAppById(jobId);
        Assert.isTrue(job != null, "job " + jobId + " must be exist");
        Assert.isTrue(jobKey != null, "job key " + jobId + " must be exist");
        Assert.isTrue(jobConfig != null, "job config" + jobId + " must be exist");
        Assert.isTrue(app != null, "job" + jobId + " 'app must be exist");
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
