package me.stevenkin.boom.job.processor.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.*;
import me.stevenkin.boom.job.common.enums.JobFireResult;
import me.stevenkin.boom.job.common.job.JobExecReportService;
import me.stevenkin.boom.job.common.job.JobProcessor;
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

    private JobExecReportService jobExecReportService;

    public SimpleJobProcessor(Job job, String jobId, BoomJobClient jobClient) {
        this.job = job;
        this.jobId = jobId;
        this.jobClient = jobClient;
        this.executor = jobClient.executor();
        this.jobExecReportService = jobClient.jobExecReportService();
    }

    @Override
    public JobFireResponse fireJob(JobFireRequest request) {
        if (!request.getJobId().equals(jobId)) {
            return new JobFireResponse(JobFireResult.FIRE_FAILED, jobClient.clientId());
        }
        executor.submit(() -> {
            log.info("job {} is fired", jobId);
            JobResult result;
            Throwable error = null;
            Instant startTime = Instant.now();
            Instant endTime;
            try {
                result = job.execute(buildJobContext(request));
                endTime = Instant.now();
            } catch (Throwable throwable) {
                result = JobResult.FAIL;
                error = throwable;
                endTime = Instant.now();
                log.error("some error happen when job {} is executing", jobId, throwable);
            }
            JobExecReport jobExecReport = new JobExecReport();
            BeanUtils.copyProperties(request, jobExecReport);
            jobExecReport.setClientId(jobClient.clientId());
            jobExecReport.setJobResult(result);
            jobExecReport.setException(error);
            jobExecReport.setStartTime(startTime);
            jobExecReport.setEndTime(endTime);
            jobExecReportService.reportJobExecResult(jobExecReport);
        });
        return new JobFireResponse(JobFireResult.FIRE_SUCCESS, jobClient.clientId());
    }

    private JobContext buildJobContext(JobFireRequest request) {
        JobContext jobContext = new JobContext();
        BeanUtils.copyProperties(request, jobContext);
        return jobContext;
    }

}
