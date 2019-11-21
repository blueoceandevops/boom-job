package me.stevenkin.boom.job.common.service;

public interface JobSchedulerService {

    Boolean onlineJob(Long jobId);

    Long triggerJob(Long jobId);

    Long onlineAndTriggerJob(Long jobId);

    Boolean pauseJob(Long jobId);

    Boolean resumeJob(Long jobId);

    Boolean offlineJob(Long jobId);

    Boolean reloadJob(Long jobId);

    Boolean failoverJob(Long jobId, String schedulerId);

}
