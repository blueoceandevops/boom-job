package me.stevenkin.boom.job.scheduler.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.data.dao.JobInstanceShardDao;
import me.stevenkin.boom.job.common.service.ShardExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@Component
public class ShardExecuteServiceImpl implements ShardExecuteService {

    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;

    @Override
    public Boolean insertShardExecTurnover(Long jobShardId, String clientId, Instant instant) {
        try {
            jobInstanceShardDao.insertShardExecTurnover(jobShardId, clientId, instant);
        }catch (Exception e) {
            log.error("insert shard execute turnover failed", e);
            return false;
        }
        return true;
    }
}
