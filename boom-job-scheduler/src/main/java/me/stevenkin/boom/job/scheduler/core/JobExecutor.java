package me.stevenkin.boom.job.scheduler.core;

import me.stevenkin.boom.job.common.dto.JobDetail;
import me.stevenkin.boom.job.common.service.JobProcessor;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class JobExecutor {

    public void execute(JobDetail jobDetail, JobProcessor jobProcessor, JobExecutionContext context) {

    }

}
