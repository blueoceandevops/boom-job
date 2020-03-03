package me.stevenkin.boom.job.common.service;

public interface FailoverService {

    void processClientFailed(String clientId);

    void processSchedulerFailed(String schedulerId);

}
