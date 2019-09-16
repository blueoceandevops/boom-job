package me.stevenkin.boom.job.storage.dao;

import me.stevenkin.boom.job.common.po.JobConfig;

public interface JobConfigDao {

    JobConfig selectById(Long id);

    JobConfig selectByJobId(Long jobId);

    Integer insertOrUpdate(JobConfig jobConfig);

}
