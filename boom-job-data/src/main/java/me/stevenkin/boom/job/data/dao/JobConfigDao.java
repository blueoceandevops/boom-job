package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.model.JobConfig;

public interface JobConfigDao {

    JobConfig selectById(Long id);

    JobConfig selectByJobId(Long jobId);

}
