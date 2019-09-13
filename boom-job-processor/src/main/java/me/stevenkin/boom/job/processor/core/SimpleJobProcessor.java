package me.stevenkin.boom.job.processor.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.*;
import me.stevenkin.boom.job.common.enums.JobFireResult;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.common.service.ShardExecuteService;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

@Getter
@NoArgsConstructor
@Slf4j
public class SimpleJobProcessor implements JobProcessor {

    private Job job;

    private String jobId;

    private BoomJobClient jobClient;

    private ExecutorService executor;

    private JobExecuteService jobExecuteService;

    private ShardExecuteService shardExecuteService;

    public SimpleJobProcessor(Job job, String jobId, BoomJobClient jobClient) {
        this.job = job;
        this.jobId = jobId;
        this.jobClient = jobClient;
        this.executor = jobClient.executor();
        this.jobExecuteService = jobClient.jobExecuteService();
        this.shardExecuteService = jobClient.shardExecuteService();
    }

    @Override
    public JobFireResponse fireJob(JobFireRequest request) {
        if (!request.getJobKey().equals(jobId)) {
            return new JobFireResponse(JobFireResult.FIRE_FAILED, jobClient.clientId());
        }
        Long jobInstanceId = request.getJobInstanceId();
        List<Long> jobShardIds = request.getJobShardIds();
        Queue<Long> shardIds = new LinkedList<>();
        shardIds.addAll(jobShardIds);
        executor.submit(() -> {
            log.info("service {} is fired", jobId);
            while (!jobExecuteService.checkJobInstanceIsFinish(jobInstanceId)) {
                Long id;
                while ((id = shardIds.poll()) != null) {
                    JobInstanceShardDto jobInstanceShardDto = jobExecuteService.fetchOneShard(new FetchShardRequest(id, jobClient.clientId()));
                    if (jobInstanceShardDto != null) {
                        jobExecuteService.reportJobExecResult(executeJobShard(jobInstanceShardDto));
                    }
                }
                shardIds.addAll(jobExecuteService.fetchMoreShardIds(jobInstanceId));
            }
        });
        return new JobFireResponse(JobFireResult.FIRE_SUCCESS, jobClient.clientId());
    }

    private JobExecReport executeJobShard(JobInstanceShardDto dto) {
        JobResult result;
        Throwable error = null;
        Instant startTime = Instant.now();
        Instant endTime;
        try {
            result = job.execute(buildJobContext(dto));
            endTime = Instant.now();
        } catch (Throwable throwable) {
            result = JobResult.FAIL;
            error = throwable;
            endTime = Instant.now();
            log.error("some error happen when service {} is executing", jobId, throwable);
        }
        Long execTime = endTime.toEpochMilli() - startTime.toEpochMilli();
        JobExecReport jobExecReport = new JobExecReport();
        jobExecReport.setJobKey(jobId);
        jobExecReport.setJobInstanceId(dto.getJobInstanceId());
        jobExecReport.setJobShardId(dto.getJobShardId());
        jobExecReport.setClientId(jobClient.clientId());
        jobExecReport.setExecuteTime(execTime);
        jobExecReport.setJobResult(result);
        jobExecReport.setException(error);
        return jobExecReport;
    }

    private JobContext buildJobContext(JobInstanceShardDto dto) {
        JobContext jobContext = new JobContext();
        BeanUtils.copyProperties(dto, jobContext);
        return jobContext;
    }

}
