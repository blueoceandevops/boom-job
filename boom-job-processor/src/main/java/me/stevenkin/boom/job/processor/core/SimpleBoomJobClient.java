package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import me.stevenkin.boom.job.common.job.AppRegisterService;
import me.stevenkin.boom.job.common.job.JobProcessor;
import me.stevenkin.boom.job.common.job.RegisterService;
import me.stevenkin.boom.job.common.kit.ExecutorKit;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.kit.SystemKit;
import me.stevenkin.boom.job.common.support.Lifecycle;

import java.util.concurrent.ExecutorService;

public class SimpleBoomJobClient implements BoomJobClient, Lifecycle{

    private String appName;

    private String author;

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

    private RegisterService registerService;

    private ReferenceConfig<RegisterService> reference;

    private ReferenceConfigCache cache = ReferenceConfigCache.getCache();

    public SimpleBoomJobClient(String appName, String author, String appSecret, String zkHosts, String namespace, Integer executeThreadCount) {
        this.appName = appName;
        this.author = author;
        this.appSecret = appSecret;
        this.zkHosts = zkHosts;
        this.namespace = namespace;
        this.executeThreadCount = executeThreadCount;
        this.clientRegister = new ClientRegister(this);
        this.executor = ExecutorKit.newExecutor(executeThreadCount, 10000, "job-executor-");
        this.jobPool = new JobPool(this);
        this.application.setName(NameKit.getAppId(appName, author));
        this.registry.setAddress(zkHosts);
        this.registry.setProtocol("zookeeper");
        this.reference = new ReferenceConfig<>();
        this.reference.setApplication(application);
        this.reference.setRegistry(registry);
        this.reference.setInterface(RegisterService.class);
        this.registerService = cache.get(reference);
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
    public RegisterService registerService() {
        return registerService;
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
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void shutdown() {
        clientRegister.shutdown();
        jobPool.shutdown();
    }
}