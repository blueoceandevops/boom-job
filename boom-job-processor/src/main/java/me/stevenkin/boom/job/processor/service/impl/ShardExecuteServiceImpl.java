package me.stevenkin.boom.job.processor.service.impl;

import me.stevenkin.boom.job.data.dao.JobInstanceShardMapper;
import me.stevenkin.boom.job.processor.service.ShardExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ShardExecuteServiceImpl implements ShardExecuteService {

    @Autowired
    private JobInstanceShardMapper jobInstanceShardMapper;

    @Override
    public Boolean insertShardExecTurnover(Long jobShardId, String clientId, Instant instant) {
        try {
            jobInstanceShardMapper.insertShardExecTurnover(jobShardId, clientId, instant);
        }catch (Exception e) {
            return false;
        }
        return true;
    }
}
