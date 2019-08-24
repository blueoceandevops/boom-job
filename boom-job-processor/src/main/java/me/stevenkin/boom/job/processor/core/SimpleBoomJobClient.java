package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.common.kit.ExecutorKit;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.kit.SystemKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.service.ShardExecuteService;

import java.util.concurrent.ExecutorService;

public class SimpleBoomJobClient implements BoomJobClient, Lifecycle{

    private String appName;

    private String author;

    private String version;

    private String appSecret;

    private String zkHosts;

    private String namespace;

    private String clientId = SystemKit.hostPid();

    private Integer executeThreadCount;

    private JobPool jobPool;

    private ClientRegister clientRegister;

    private ExecutorService executor;

    private ApplicationConfig application = new ApplicationConfig();

    private RegistryConfig registry = new RegistryConfig();

    private ProtocolConfig protocol = new ProtocolConfig();

    private RegisterService registerService;

    private JobExecuteService jobExecuteService;

    private ShardExecuteService shardExecuteService;

    private ReferenceConfig<RegisterService> referRegister;

    private ReferenceConfig<JobExecuteService> referJob;

    private ReferenceConfig<ShardExecuteService> referShard;

    private ReferenceConfigCache cache = ReferenceConfigCache.getCache();

    public SimpleBoomJobClient(String appName, String author, String version, String appSecret, String zkHosts, String zkUsername, String zkPassword, String namespace, Integer timeout, Integer executeThreadCount) {
        this.appName = appName;
        this.author = author;
        this.version = version;
        this.appSecret = appSecret;
        this.zkHosts = zkHosts;
        this.namespace = namespace;
        this.executeThreadCount = executeThreadCount;
        this.executor = ExecutorKit.newExecutor(executeThreadCount, 10000, "service-executor-");
        this.application.setName(NameKit.getAppId(appName, author, version));
        this.registry.setAddress(zkHosts);
        this.registry.setProtocol("zookeeper");
        this.registry.setUsername(zkUsername);
        this.registry.setPassword(zkPassword);
        this.protocol.setName("dubbo");
        this.protocol.setPort(-1);
        this.protocol.setThreads(200);

        this.referRegister = new ReferenceConfig<>();
        this.referJob = new ReferenceConfig<>();
        this.referShard = new ReferenceConfig<>();

        this.referRegister.setTimeout(timeout);
        this.referJob.setTimeout(timeout);
        this.referShard.setTimeout(timeout);

        this.referRegister.setRetries(0);
        this.referJob.setRetries(0);
        this.referShard.setRetries(0);

        this.referRegister.setApplication(application);
        this.referRegister.setRegistry(registry);
        this.referRegister.setInterface(RegisterService.class);

        this.referJob.setApplication(application);
        this.referJob.setRegistry(registry);
        this.referJob.setInterface(JobExecuteService.class);

        this.referShard.setApplication(application);
        this.referShard.setRegistry(registry);
        this.referShard.setInterface(ShardExecuteService.class);

        this.registerService = cache.get(referRegister);
        this.jobExecuteService = cache.get(referJob);
        this.shardExecuteService = cache.get(referShard);

        this.jobPool = new JobPool(this);
        this.clientRegister = new ClientRegister(this);
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
    public String version() {
        return version;
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
    public RegisterService registerService() {
        return registerService;
    }

    @Override
    public JobExecuteService jobExecuteService() {
        return jobExecuteService;
    }

    @Override
    public ShardExecuteService shardExecuteService() {
        return shardExecuteService;
    }

    @Override
    public ExecutorService executor() {
        return executor;
    }

    @Override
    public JobProcessor getJobProcessor(Class<? extends Job> jobClass) {
        return jobPool.getJobProcessor(jobClass);
    }

    @Override
    public void registerJob(Job job) {
        jobPool.registerJob(job);
    }

    @Override
    public void start() {
        clientRegister.start();
        jobPool.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "BoomShutdownHook"));
    }

    @Override
    public void shutdown() {
        clientRegister.shutdown();
        jobPool.shutdown();
        cache.destroy(referRegister);
        cache.destroy(referJob);
        cache.destroy(referShard);
    }
}
