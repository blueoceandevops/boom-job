package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.dto.FetchShardRequest;
import me.stevenkin.boom.job.common.dto.FetchShardResponse;
import me.stevenkin.boom.job.common.dto.JobExecReport;
import me.stevenkin.boom.job.common.dto.JobInstanceShardDto;

import java.util.List;

public interface JobExecuteService {

    JobInstanceShardDto fetchOneShard(FetchShardRequest request);

    List<Long> fetchMoreShardIds(Long jobInstance);

    Boolean checkJobInstanceIsFinal(Long jobInstance);

    void reportJobExecResult(JobExecReport jobExecReport);

}
