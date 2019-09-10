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
        if (!request.getJobId().equals(jobId)) {
            return new JobFireResponse(JobFireResult.FIRE_FAILED, jobClient.clientId());
        }
        Long jobInstanceId = request.getJobInstanceId();
        executor.submit(() -> {
            log.info("service {} is fired", jobId);
            Long fetchShardCount = request.getFetchShardCount();
            boolean first = true;
            do{
                if (!first) {
                    fetchShardCount += jobExecuteService.fetchMoreShardCount(jobInstanceId);
                }
                if (first) {
                    first = false;
                }
                while (fetchShardCount > 0) {
                    FetchShardResponse response = jobExecuteService.fetchOneShard(jobInstanceId);
                    if (response == null) {
                        continue;
                    }
                    if (response.getInstanceIsFinal()) {
                        break;
                    }
                    if (response.getJobInstanceShard() == null) {
                        continue;
                    }
                    JobInstanceShardDto jobInstanceShardDto = response.getJobInstanceShard();
                    if (!shardExecuteService.insertShardExecTurnover(jobInstanceShardDto.getJobShardId(),
                            jobClient.clientId(), Instant.now()))
                        continue;
                    JobExecReport jobExecReport = executeJobShard(jobInstanceShardDto);
                    jobExecuteService.reportJobExecResult(jobExecReport);
                    fetchShardCount--;
                }
            } while (!jobExecuteService.checkJobInstanceIsFinal(jobInstanceId));
        });
        return new JobFireResponse(JobFireResult.FIRE_SUCCESS, jobClient.clientId());
    }

    private JobExecReport executeJobShard(JobInstanceShardDto shardVo) {
        JobResult result;
        Throwable error = null;
        Instant startTime = Instant.now();
        Instant endTime;
        try {
            result = job.execute(buildJobContext(shardVo));
            endTime = Instant.now();
        } catch (Throwable throwable) {
            result = JobResult.FAIL;
            error = throwable;
            endTime = Instant.now();
            log.error("some error happen when service {} is executing", jobId, throwable);
        }
        JobExecReport jobExecReport = new JobExecReport();
        BeanUtils.copyProperties(shardVo, jobExecReport);
        jobExecReport.setClientId(jobClient.clientId());
        jobExecReport.setJobResult(result);
        jobExecReport.setException(error);
        jobExecReport.setStartTime(startTime);
        jobExecReport.setEndTime(endTime);
        return jobExecReport;
    }

    private JobContext buildJobContext(JobInstanceShardDto request) {
        JobContext jobContext = new JobContext();
        BeanUtils.copyProperties(request, jobContext);
        return jobContext;
    }

}
