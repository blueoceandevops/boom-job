package me.stevenkin.boom.job.common.job;

import me.stevenkin.boom.job.common.bean.JobExecReport;
import me.stevenkin.boom.job.common.bean.JobFireRequest;
import me.stevenkin.boom.job.common.bean.JobFireResponse;
import me.stevenkin.boom.job.common.support.Lifecycle;

public interface JobProcessor {

    /**
     * fire this job
     * @param request
     * @return JobFireResponse
     */
    JobFireResponse fireJob(JobFireRequest request);
}
