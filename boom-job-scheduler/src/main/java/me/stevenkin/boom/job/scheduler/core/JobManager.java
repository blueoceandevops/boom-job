package me.stevenkin.boom.job.scheduler.core;

import me.stevenkin.boom.job.common.enums.JobStatus;
import me.stevenkin.boom.job.common.model.Job;
import me.stevenkin.boom.job.data.dao.JobInfoDao;
import me.stevenkin.boom.job.data.dao.JobScheduleDao;
import me.stevenkin.boom.job.scheduler.SchedulerContext;
import me.stevenkin.boom.job.scheduler.config.BoomJobConfig;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobManager implements InitializingBean {
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private JobScheduleDao jobScheduleDao;
    @Autowired
    private SchedulerContext schedulerContext;
    @Autowired
    private BoomJobConfig config;

    private SchedulerFactory schedulers;

    private Map<Long, ScheduledJob> jobCaches = new ConcurrentHashMap<>();

    public Boolean onlineJob(Long jobId){
        Job job = jobInfoDao.selectById(jobId);
        if (job == null) {
            return Boolean.FALSE;
        }
        jobScheduleDao.insertJobScheduleTurnover(jobId, schedulerContext.getSchedulerId(), Instant.now());
        Job job1 = new Job();
        job1.setId(jobId);
        job1.setStatus(JobStatus.ONLINE.getCode());
        job1.setUpdateTime(LocalDateTime.now());
        int count = jobInfoDao.updateWhereStatusIs(JobStatus.OFFLINE.getCode(), job1);
        if (count <= 0) {
            throw new IllegalStateException();
        }
        ScheduledJob job2 = new ScheduledJob();
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
}
