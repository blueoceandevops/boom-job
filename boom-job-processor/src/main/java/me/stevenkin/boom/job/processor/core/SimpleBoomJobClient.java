package me.stevenkin.boom.job.processor.core;

import me.stevenkin.boom.job.common.job.JobProcessor;
import me.stevenkin.boom.job.common.kit.ExecutorKit;
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

    public SimpleBoomJobClient(String appName, String appSecret, String zkHosts, String namespace, Integer executeThreadCount) {
        this.appName = appName;
        this.appSecret = appSecret;
        this.zkHosts = zkHosts;
        this.namespace = namespace;
        this.executeThreadCount = executeThreadCount;
        this.clientRegister = new ClientRegister(zkHosts, namespace, clientId);
        this.executor = ExecutorKit.newExecutor(executeThreadCount, 10000, "job-executor-");
        this.jobPool = new JobPool(this);
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
