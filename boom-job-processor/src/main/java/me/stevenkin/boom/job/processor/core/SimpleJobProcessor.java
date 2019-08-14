package me.stevenkin.boom.job.processor.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.bean.JobExecReport;
import me.stevenkin.boom.job.common.bean.JobFireRequest;
import me.stevenkin.boom.job.common.bean.JobFireResponse;
import me.stevenkin.boom.job.common.job.JobProcessor;

import java.util.concurrent.ExecutorService;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleJobProcessor implements JobProcessor {

    private Job job;

    private String jobId;

    private String jobVersion;

    private ExecutorService executor;

    @Override
    public JobFireResponse fireJob(JobFireRequest request) {
        return null;
    }

    @Override
    public void reportJobExecInfo(JobExecReport report) {

    }
}
