package me.stevenkin.boom.job;

import me.stevenkin.boom.job.processor.service.ShardExecuteService;

import java.time.Instant;

public class ShardExecuteServiceTest implements ShardExecuteService {
    @Override
    public Boolean insertShardExecTurnover(Long jobShardId, String clientId, Instant instant) {
        return true;
    }
}
