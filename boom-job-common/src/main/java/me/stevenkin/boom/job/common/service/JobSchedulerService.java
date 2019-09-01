package me.stevenkin.boom.job.common.service;

public interface JobSchedulerService {

    Boolean onlineJob(Long jobId);

    Boolean triggerJob(Long jobId);

    Boolean pauseJob(Long jobId);

    Boolean resumeJob(Long jobId);

    Boolean offlineJob(Long jobId);

    Boolean reloadJob(Long jobId);

}