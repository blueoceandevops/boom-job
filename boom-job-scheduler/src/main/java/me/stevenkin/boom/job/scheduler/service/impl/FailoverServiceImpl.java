package me.stevenkin.boom.job.scheduler.service.impl;

import me.stevenkin.boom.job.scheduler.service.FailoverService;
import org.springframework.stereotype.Component;

@Component
public class FailoverServiceImpl implements FailoverService {
    @Override
    public void processClientFailed(String clientId) {

    }

    @Override
    public void processSchedulerFailed(String schedulerId) {

    }
}
