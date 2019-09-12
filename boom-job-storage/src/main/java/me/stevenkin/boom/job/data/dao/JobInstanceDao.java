package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.po.JobInstance;

import java.time.LocalDateTime;
import java.util.List;

public interface JobInstanceDao {

    Integer insertJobInstance(JobInstance jobInstance);

    Integer insertJobInstanceOnlyAllowOneRunning(JobInstance jobInstance);

    Integer updateJobInstanceStatus(Long id, Integer status);

    JobInstance selectById(Long id);

    Integer countFinalById(Long id);

    List<JobInstance> selectByJobId(Long jobId);

    List<JobInstance> selectAll();

    List<JobInstance> selectFinishedBefore(LocalDateTime time);

    Integer deleteById(Long id);

}
