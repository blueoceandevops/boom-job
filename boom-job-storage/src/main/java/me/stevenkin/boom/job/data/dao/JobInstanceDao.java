package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.po.JobInstance;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface JobInstanceDao {

    Integer insertJobInstance(JobInstance jobInstance);

    Integer insertJobInstanceOnlyAllowOneRunning(JobInstance jobInstance);

    Integer updateJobInstanceStatus(Long id, Integer expectStatus, Integer status);

    JobInstance selectById(Long id);

    Integer countFinalById(Long id);

    List<JobInstance> selectByJobId(Long jobId);

    List<JobInstance> selectAll();

    List<JobInstance> selectFinishedBefore(LocalDateTime time);

    Integer deleteById(Long id);

}
