package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.registry.RegistryService;
import me.stevenkin.boom.job.common.service.*;
import me.stevenkin.boom.job.common.kit.ExecutorKit;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.CommandProcessor;
import me.stevenkin.boom.job.common.zk.ZkClient;

import java.util.concurrent.ExecutorService;

/**
 * 1. new SimpleBoomJobClient
 * 2. start
 * 3. register jobs
 */
public class SimpleBoomJobClient extends Lifecycle implements BoomJobClient {
    private static final String CMD_CLIENT = "command/client";

    private String appName;

    private String author;

    private String appSecret;

    private String zkHosts;

    private String namespace;

    private ZkClient zkClient;

    private String clientId;

    private Integer executeThreadCount;

    private Integer timeout;

    private JobPool jobPool;

    private SimpleClientProcessor clientProcessor;

    private CommandProcessor commandProcessor;

    private ExecutorService executor;

    private ApplicationConfig application = new ApplicationConfig();

    private RegistryConfig registry = new RegistryConfig();

    private ProtocolConfig protocol = new ProtocolConfig();

    private AppRegisterService appRegisterService;

    private JobExecuteService jobExecuteService;

    private RegistryService registryService;

    private ReferenceConfig<AppRegisterService> referRegister;

    private ReferenceConfig<JobExecuteService> referJob;

    private ReferenceConfig<RegistryService> referRegistry;

    private ReferenceConfigCache cache = ReferenceConfigCache.getCache();

    public SimpleBoomJobClient(String appName, String author, String appSecret, String zkHosts, String zkUsername, String zkPassword, String namespace, Integer timeout, Integer executeThreadCount) throws Exception {
        this.appName = appName;
        this.author = author;
        this.appSecret = appSecret;

        this.zkHosts = zkHosts;
        this.namespace = namespace;
        this.zkClient = new ZkClient(zkHosts, namespace);

        this.executeThreadCount = executeThreadCount;
        this.executor = ExecutorKit.newExecutor(executeThreadCount, 10000, "service-executor-");

        this.timeout = timeout;

        this.application.setName(NameKit.getAppKey(appName, author));
        this.registry.setAddress(zkHosts);
        this.registry.setProtocol("zookeeper");
        this.registry.setUsername(zkUsername);
        this.registry.setPassword(zkPassword);
        this.protocol.setName("dubbo");
        this.protocol.setPort(-1);
        this.protocol.setThreads(200);
    }

    @Override
    public String appName() {
        return appName;
    }

    @Override
    public String author() {
        return author;
    }

    @Override
    public String appKey() {
        return author + "_" + appName;
    }

    @Override
    public String appSecret() {
        return appSecret;
    }

    @Override
    public String clientId() {
        return clientId;
    }

    @Override
    public String zkHosts() {
        return zkHosts;
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public ZkClient zkClient() {
        return zkClient;
    }

    @Override
    public Integer executeThreadCount() {
        return executeThreadCount;
    }

    @Override
    public ApplicationConfig applicationConfig() {
        return application;
    }

    @Override
    public RegistryConfig registerConfig() {
        return registry;
    }

    @Override
    public ProtocolConfig protocolConfig() {
        return protocol;
    }

    @Override
    public AppRegisterService registerService() {
        return appRegisterService;
    }

    @Override
    public JobExecuteService jobExecuteService() {
        return jobExecuteService;
    }

    @Override
    public RegistryService registryService() {
        return registryService;
    }

    @Override
    public JobPool jobPool() {
        return jobPool;
    }

    @Override
    public ExecutorService executor() {
        return executor;
    }

    @Override
    public void registerJob(Job job) {
        jobPool.registerJob(job);
    }

    @Override
    public void doStart() throws Exception {
        zkClient.start();
        referService();
        jobPool = new JobPool(this);
        jobPool.start();
        clientProcessor = new SimpleClientProcessor(this);
        clientProcessor.start();
        commandProcessor = new CommandProcessor();
        commandProcessor.setComponent(this);
        commandProcessor.setZkClient(zkClient);
        commandProcessor.setCommandPath(CMD_CLIENT);
        commandProcessor.setId(clientProcessor.getClientId());
        commandProcessor.start();

    }

    private void referService() {
        this.referRegister = new ReferenceConfig<>();
        this.referJob = new ReferenceConfig<>();
        this.referRegistry = new ReferenceConfig<>();

        this.referRegister.setTimeout(this.timeout);
        this.referJob.setTimeout(this.timeout);

        this.referRegister.setRetries(0);
        this.referJob.setRetries(0);

        this.referRegister.setApplication(application);
        this.referRegister.setRegistry(registry);
        this.referRegister.setInterface(AppRegisterService.class);

        this.referJob.setApplication(application);
        this.referJob.setRegistry(registry);
        this.referJob.setInterface(JobExecuteService.class);

        this.referRegistry.setApplication(application);
        this.referRegistry.setRegistry(registry);
        this.referRegistry.setInterface(RegistryService.class);

        this.appRegisterService = cache.get(referRegister);
        this.jobExecuteService = cache.get(referJob);
        this.registryService = cache.get(referRegistry);
    }

    @Override
    public void doPause() throws Exception {
        jobPool.pause();
        clientProcessor.pause();
        commandProcessor.pause();
    }

    @Override
    public void doResume() throws Exception {
        jobPool.resume();
        clientProcessor.resume();
        commandProcessor.resume();
    }

    @Override
    public void doShutdown() throws Exception {
        clientProcessor.shutdown();
        executor.shutdownNow();
        jobPool.shutdown();
        commandProcessor.shutdown();
        cache.destroy(referRegister);
        cache.destroy(referJob);
        zkClient.shutdown();
    }
}
