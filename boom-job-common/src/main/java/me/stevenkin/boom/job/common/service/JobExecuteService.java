package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.bean.FetchShardResponse;
import me.stevenkin.boom.job.common.bean.JobExecReport;

public interface JobExecuteService {

    FetchShardResponse fetchOneShard(Long jobInstance);

    Long fetchMoreShardCount(Long jobInstance);

    Boolean checkJobInstanceIsFinal(Long jobInstance);

    void reportJobExecResult(JobExecReport jobExecReport);

}
