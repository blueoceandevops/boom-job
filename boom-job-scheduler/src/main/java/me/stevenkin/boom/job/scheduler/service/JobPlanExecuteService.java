package me.stevenkin.boom.job.scheduler.service;

public interface JobPlanExecuteService {

    Boolean hasExecuted(Long planJobInstanceId, Long jobId);

    Boolean scheduleAndFireNow(Long planJobInstanceId, Long jobId);
}
