package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.processor.service.ShardExecuteService;

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

    JobExecuteService jobExecuteService();

    ShardExecuteService shardExecuteService();

    ExecutorService executor();

    JobProcessor getJobProcessor(Class<? extends Job> jobClass);

    void registerJob(Job job);
}
