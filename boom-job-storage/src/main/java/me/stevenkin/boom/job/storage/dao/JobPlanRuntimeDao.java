package me.stevenkin.boom.job.storage.dao;

import me.stevenkin.boom.job.common.po.JobPlanRuntime;

import java.util.List;

public interface JobPlanRuntimeDao {

    Integer insertJobPlanRuntime(JobPlanRuntime jobPlanRuntime);

    Integer updateJobPlanRuntime(JobPlanRuntime jobPlanRuntime);

    Integer deleteById(Long id);

    Integer deleteByPlanJobInstanceId(Long planJobInstanceId);

    JobPlanRuntime selectById(Long id);

    List<JobPlanRuntime> selectByPlanJobInstanceId(Long planJobInstanceId);

    JobPlanRuntime selectByPlanJobInstanceIdAndJobId(Long planJobInstanceId, Long jobId);
}
