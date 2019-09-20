package me.stevenkin.boom.job.scheduler.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.dto.JobDetail;
import me.stevenkin.boom.job.common.service.JobProcessor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyJob implements Job {
    private JobDetail jobDetail;

    private JobProcessor jobProcessor;

    private JobExecutor jobExecutor;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            jobExecutor.execute(jobDetail, jobProcessor, context);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
