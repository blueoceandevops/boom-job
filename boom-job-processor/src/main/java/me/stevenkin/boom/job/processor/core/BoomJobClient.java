package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.common.zk.ZkClient;

import java.util.concurrent.ExecutorService;

public interface BoomJobClient {

    String appName();

    String author();

    String appKey();

    String appSecret();

    String clientId();

    String zkHosts();

    String namespace();

    ZkClient zkClient();

    Integer executeThreadCount();

    ApplicationConfig applicationConfig();

    RegistryConfig registerConfig();

    ProtocolConfig protocolConfig();

    RegisterService registerService();

    JobExecuteService jobExecuteService();

    JobPool jobPool();

    ExecutorService executor();

    void registerJob(Job job);
}
