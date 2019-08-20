package me.stevenkin.boom.job.data.dao;

import org.apache.ibatis.annotations.Mapper;

import java.time.Instant;

@Mapper
public interface JobInstanceShardMapper {

    Integer insertShardExecTurnover(Long jobShardId, String clientId, Instant instant);

}
