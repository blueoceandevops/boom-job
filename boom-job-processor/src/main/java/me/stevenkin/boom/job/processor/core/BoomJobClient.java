package me.stevenkin.boom.job.processor.core;

import me.stevenkin.boom.job.common.job.JobProcessor;

import java.util.concurrent.ExecutorService;

public interface BoomJobClient {

    String appName();

    String author();

    String appSecret();

    String clientId();

    String zkHosts();

    String namespace();

    Integer executeThreadCount();

    ExecutorService executor();

    JobProcessor getJobProcessor(Class<? extends Job> jobClass);

    void registerJob(Job job);
}
