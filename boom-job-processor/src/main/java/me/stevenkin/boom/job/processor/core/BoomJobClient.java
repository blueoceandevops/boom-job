package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import me.stevenkin.boom.job.common.job.JobExecReportService;
import me.stevenkin.boom.job.common.job.JobProcessor;
import me.stevenkin.boom.job.common.job.RegisterService;
import me.stevenkin.boom.job.common.support.Lifecycle;

import java.util.concurrent.ExecutorService;

public interface BoomJobClient extends Lifecycle {

    String appName();

    String author();

    String version();

    String appSecret();

    String clientId();

    String zkHosts();

    String namespace();

    Integer executeThreadCount();

    ApplicationConfig applicationConfig();

    RegistryConfig registerConfig();

    ProtocolConfig protocolConfig();

    RegisterService registerService();

    JobExecReportService jobExecReportService();

    ExecutorService executor();

    JobProcessor getJobProcessor(Class<? extends Job> jobClass);

    void registerJob(Job job);
}
