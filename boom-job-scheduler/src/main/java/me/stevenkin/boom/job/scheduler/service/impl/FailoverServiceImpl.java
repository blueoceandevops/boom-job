package me.stevenkin.boom.job.scheduler.service.impl;

import me.stevenkin.boom.job.scheduler.service.FailoverService;
import me.stevenkin.boom.job.storage.dao.JobInfoDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceShardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FailoverServiceImpl implements FailoverService {
    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;
    @Autowired
    private JobInfoDao jobInfoDao;

    @Override
    public void processClientFailed(String clientId) {

    }

    @Override
    public void processSchedulerFailed(String schedulerId) {

    }
}
