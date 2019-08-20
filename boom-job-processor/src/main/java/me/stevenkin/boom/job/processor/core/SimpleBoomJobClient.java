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
import me.stevenkin.boom.job.processor.service.ShardExecuteService;
import org.springframework.beans.factory.annotation.Autowired;

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

    private ReferenceConfig<RegisterService> referRegister;

    private ReferenceConfig<JobExecuteService> referReport;

    private ReferenceConfigCache cache = ReferenceConfigCache.getCache();

    private ShardExecuteService shardExecuteService;

    public SimpleBoomJobClient(String appName, String author, String version, String appSecret, String zkHosts, String namespace, Integer executeThreadCount, ShardExecuteService shardExecuteService) {
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
        this.protocol.setName("dubbo");
        this.protocol.setPort(-1);
        this.protocol.setThreads(200);
        this.referRegister = new ReferenceConfig<>();
        this.referReport = new ReferenceConfig<>();
        this.referRegister.setApplication(application);
        this.referRegister.setRegistry(registry);
        this.referRegister.setInterface(RegisterService.class);
        this.referReport.setApplication(application);
        this.referReport.setRegistry(registry);
        this.referReport.setInterface(JobExecuteService.class);
        this.registerService = cache.get(referRegister);
        this.jobExecuteService = cache.get(referReport);

        this.shardExecuteService = shardExecuteService;

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
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void shutdown() {
        clientRegister.shutdown();
        jobPool.shutdown();
        cache.destroy(referRegister);
        cache.destroy(referReport);
    }
}
