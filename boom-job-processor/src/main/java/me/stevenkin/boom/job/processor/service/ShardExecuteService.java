package me.stevenkin.boom.job.processor.service;

import java.time.Instant;

public interface ShardExecuteService {

    Boolean insertShardExecTurnover(Long jobShardId, String clientId, Instant instant);

}
