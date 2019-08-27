package me.stevenkin.boom.job.scheduler.service;

public interface FailoverService {

    void processClientFailed(String clientId);

    void processSchedulerFailed(String schedulerId);

}
