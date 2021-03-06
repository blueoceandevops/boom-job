package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobConfig;
import me.stevenkin.boom.job.common.po.JobPlan;
import me.stevenkin.boom.job.common.vo.JobConfigVo;

import java.util.List;

public interface JobAdminService {
    //job admin manage
    List<Job> listJobPagingByApp(Long appId, Integer pageNum, Integer pageSize);

    Job getJobById(Long jobId);

    JobConfig getJobConfigByJobId(Long jobId);

    List<JobPlan> getJobPlanByJobId(Long jobId);

    Boolean saveJob(JobConfigVo jobConfigVo);

    Boolean deleteJobAndConfig(Long jobId);

    //job control
    Boolean onlineJob(Long jobId);

    Boolean triggerJob(Long jobId);

    Boolean pauseJob(Long jobId);

    Boolean resumeJob(Long jobId);

    Boolean offlineJob(Long jobId);
}
