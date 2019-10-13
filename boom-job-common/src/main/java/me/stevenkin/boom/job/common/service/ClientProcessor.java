package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.dto.JobFireRequest;
import me.stevenkin.boom.job.common.dto.JobFireResponse;

public interface ClientProcessor {

    JobFireResponse fireJob(JobFireRequest request);

}
