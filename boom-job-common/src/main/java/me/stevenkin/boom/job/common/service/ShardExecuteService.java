package me.stevenkin.boom.job.common.service;

import java.time.Instant;

public interface ShardExecuteService {

    Boolean insertShardExecTurnover(Long jobShardId, String clientId, Instant instant);

}
