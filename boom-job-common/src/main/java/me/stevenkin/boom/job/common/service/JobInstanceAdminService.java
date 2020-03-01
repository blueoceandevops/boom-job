package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.enums.JobInstanceStatus;
import me.stevenkin.boom.job.common.po.JobInstance;

import java.util.List;

public interface JobInstanceAdminService {

    JobInstance getJobInstance(Long jobInstanceId);

    List<JobInstance> getJobInstanceGroupByStatusPaging(Long jobId, JobInstanceStatus status, Integer pageNum, Integer pageSize);

    Boolean terminateJobInstance(Long jobInstanceId);

}
