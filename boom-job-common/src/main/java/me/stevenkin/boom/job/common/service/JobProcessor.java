package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.dto.JobFireRequest;
import me.stevenkin.boom.job.common.dto.JobFireResponse;

public interface JobProcessor {
    /**
     * fire this service
     * @param request
     * @return JobFireResponse
     */
    JobFireResponse fireJob(JobFireRequest request);
}
