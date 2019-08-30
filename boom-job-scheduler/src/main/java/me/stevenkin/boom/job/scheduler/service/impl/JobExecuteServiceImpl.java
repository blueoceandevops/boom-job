package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.FetchShardResponse;
import me.stevenkin.boom.job.common.bean.JobExecReport;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import org.springframework.stereotype.Component;

@Slf4j
@Service
@Component
public class JobExecuteServiceImpl implements JobExecuteService {
    @Override
    public FetchShardResponse fetchOneShard(Long jobInstance) {
        return null;
    }

    @Override
    public Long fetchMoreShardCount(Long jobInstance) {
        return null;
    }

    @Override
    public Boolean checkJobInstanceIsFinal(Long jobInstance) {
        return null;
    }

    @Override
    public void reportJobExecResult(JobExecReport jobExecReport) {

    }
}
