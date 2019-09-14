package me.stevenkin.boom.job.storage.dao;

import me.stevenkin.boom.job.common.po.JobShardExecuteLog;

import java.util.List;

public interface JobShardExecuteLogDao {
    JobShardExecuteLog selectById(Long id);

    List<JobShardExecuteLog> selectByJobInstanceId(Long jobInstanceId);

    List<JobShardExecuteLog> selectByJobInstanceShardId(Long jobShardId);

    List<JobShardExecuteLog> selectAll();

    Integer insert(JobShardExecuteLog log);

    Integer update(JobShardExecuteLog log);

    Integer deleteById(Long id);

    Integer deleteByJobInstanceId(Long jobInstanceId);

    Integer deleteByJobInstanceShardId(Long JobInstanceShardId);
}
