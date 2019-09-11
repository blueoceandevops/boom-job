package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.po.JobConfig;

public interface JobConfigDao {

    JobConfig selectById(Long id);

    JobConfig selectByJobId(Long jobId);

}
