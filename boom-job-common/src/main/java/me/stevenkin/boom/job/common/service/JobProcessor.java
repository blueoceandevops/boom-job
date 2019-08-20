package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.bean.JobFireRequest;
import me.stevenkin.boom.job.common.bean.JobFireResponse;

public interface JobProcessor {

    /**
     * fire this service
     * @param request
     * @return JobFireResponse
     */
    JobFireResponse fireJob(JobFireRequest request);
}
