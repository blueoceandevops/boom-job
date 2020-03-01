package me.stevenkin.boom.job.common.service;

public interface JobSchedulerService {

    Boolean onlineJob(Long jobId);

    Boolean triggerJob(Long jobId);

    Boolean onlineAndTriggerJob(Long jobId, Long planJobInstanceId);

    Boolean pauseJob(Long jobId);

    Boolean resumeJob(Long jobId);

    Boolean offlineJob(Long jobId);

    Boolean reloadJob(Long jobId);

    Boolean failoverJob(Long jobId, String schedulerId);

}
