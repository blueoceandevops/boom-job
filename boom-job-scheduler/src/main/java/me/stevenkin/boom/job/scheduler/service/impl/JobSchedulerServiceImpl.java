package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.service.JobSchedulerService;
import me.stevenkin.boom.job.scheduler.core.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Service
public class JobSchedulerServiceImpl implements JobSchedulerService {
    @Autowired
    private JobManager jobManager;

    @Override
    public Boolean onlineJob(Long jobId) {
        return jobManager.onlineJob(jobId);
    }

    @Override
    public Boolean triggerJob(Long jobId) {
        return jobManager.triggerJob(jobId);
    }

    @Override
    public Boolean pauseJob(Long jobId) {
        return jobManager.pauseJob(jobId);
    }

    @Override
    public Boolean resumeJob(Long jobId) {
        return jobManager.resumeJob(jobId);
    }

    @Override
    public Boolean offlineJob(Long jobId) {
        return jobManager.offlineJob(jobId);
    }

    @Override
    public Boolean reloadJob(Long jobId) {
        return jobManager.reloadJob(jobId);
    }

    @Override
    public Boolean failoverJob(Long jobId, String schedulerId) {
        return jobManager.failoverJob(jobId, schedulerId);
    }
}
