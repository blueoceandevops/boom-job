package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.po.JobInstanceShard;
import org.apache.ibatis.annotations.Mapper;

import java.time.Instant;

@Mapper
public interface JobInstanceShardDao {

    Integer insertJobInstanceShard(JobInstanceShard jobInstanceShard);

}
