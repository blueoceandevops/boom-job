package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.FetchShardRequest;
import me.stevenkin.boom.job.common.dto.FetchShardResponse;
import me.stevenkin.boom.job.common.dto.JobExecReport;
import me.stevenkin.boom.job.common.dto.JobInstanceShardDto;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Service
@Component
public class JobExecuteServiceImpl implements JobExecuteService {

    @Override
    public JobInstanceShardDto fetchOneShard(FetchShardRequest request) {
        return null;
    }

    @Override
    public List<Long> fetchMoreShardIds(Long jobInstance) {
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
