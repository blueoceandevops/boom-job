package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.po.JobInstance;

public interface JobInstanceDao {

    Integer insertJobInstance(JobInstance jobInstance);

    Integer insertJobInstanceOnlyAllowOneRunning(JobInstance jobInstance);

}
