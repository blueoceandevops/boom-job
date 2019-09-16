package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobConfig;
import me.stevenkin.boom.job.common.vo.JobConfigVo;

import java.util.List;

public interface JobAdminService {
    //TODO job admin manage...
    List<Job> listJobPagingByApp(Long appId, Integer pageNum, Integer pageSize);

    Boolean saveJob(JobConfigVo jobConfigVo);

    //job control
    Boolean onlineJob(Long jobId);

    Boolean triggerJob(Long jobId);

    Boolean pauseJob(Long jobId);

    Boolean resumeJob(Long jobId);

    Boolean offlineJob(Long jobId);
}
