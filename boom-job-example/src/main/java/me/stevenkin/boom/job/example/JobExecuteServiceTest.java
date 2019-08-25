package me.stevenkin.boom.job.example;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.FetchShardResponse;
import me.stevenkin.boom.job.common.bean.JobExecReport;
import me.stevenkin.boom.job.common.bean.JobInstanceShardVo;
import me.stevenkin.boom.job.common.service.JobExecuteService;

@Slf4j
public class JobExecuteServiceTest implements JobExecuteService {

    private Long count = 9L;

    @Override
    public FetchShardResponse fetchOneShard(Long jobInstance) {
        log.info(jobInstance.toString());
        return new FetchShardResponse(new JobInstanceShardVo("stevenkin_test_0.0.1_me.stevenkin.boom.job.TestJob",
                "default",
                0L,
                0L,
                0L,
                "",
                100L), false);
    }

    @Override
    public Long fetchMoreShardCount(Long jobInstance) {
        count -= 3L;
        return 3L;
    }

    @Override
    public Boolean checkJobInstanceIsFinal(Long jobInstance) {
        return count <= 0L;
    }

    @Override
    public void reportJobExecResult(JobExecReport jobExecReport) {
        log.info(jobExecReport.toString());
    }
}