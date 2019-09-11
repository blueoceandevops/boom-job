package me.stevenkin.boom.job.data.dao;

public interface JobScheduleDao {

    Integer lockJob(Long jobId, String schedulerId);

    Integer pauseJob(Long jobId);

    Integer resumeJob(Long jobId);

    Integer offlineJob(Long jobId);

}
