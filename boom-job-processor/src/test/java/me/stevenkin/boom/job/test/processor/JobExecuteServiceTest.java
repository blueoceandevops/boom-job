package me.stevenkin.boom.job.test.processor;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.FetchShardRequest;
import me.stevenkin.boom.job.common.dto.FetchShardResponse;
import me.stevenkin.boom.job.common.dto.JobExecReport;
import me.stevenkin.boom.job.common.dto.JobInstanceShardDto;
import me.stevenkin.boom.job.common.service.JobExecuteService;

import java.util.List;

@Slf4j
public class JobExecuteServiceTest implements JobExecuteService {

    private Long count = 9L;

    @Override
    public JobInstanceShardDto fetchOneShard(FetchShardRequest request) {
        return null;
    }

    @Override
    public List<Long> fetchMoreShardIds(Long jobInstance) {
        return null;
    }

    @Override
    public Boolean checkJobInstanceIsFinish(Long jobInstance) {
        return count <= 0L;
    }

    @Override
    public void reportJobExecResult(JobExecReport jobExecReport) {
        log.info(jobExecReport.toString());
    }
}
