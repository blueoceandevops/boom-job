package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import me.stevenkin.boom.job.common.job.JobProcessor;
import me.stevenkin.boom.job.common.job.RegisterService;

import java.util.concurrent.ExecutorService;

public interface BoomJobClient {

    String appName();

    String author();

    String appSecret();

    String clientId();

    String zkHosts();

    String namespace();

    Integer executeThreadCount();

    ApplicationConfig applicationConfig();

    RegistryConfig registerConfig();

    RegisterService registerService();

    ExecutorService executor();

    JobProcessor getJobProcessor(Class<? extends Job> jobClass);

    void registerJob(Job job);
}
