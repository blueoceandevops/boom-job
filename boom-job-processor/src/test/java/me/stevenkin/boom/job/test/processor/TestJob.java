package me.stevenkin.boom.job.test.processor;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.JobContext;
import me.stevenkin.boom.job.common.dto.JobResult;
import me.stevenkin.boom.job.processor.annotation.BoomJob;
import me.stevenkin.boom.job.processor.core.Job;

@BoomJob
@Slf4j
public class TestJob implements Job{
    @Override
    public JobResult execute(JobContext jobContext) throws Throwable {
        log.info("Test service is running");
        return JobResult.SUCCESS;
    }
}
