package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.AppInfo;
import me.stevenkin.boom.job.common.dto.JobInfo;
import me.stevenkin.boom.job.common.dto.RegisterResponse;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.support.Lifecycle;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JobPool implements Lifecycle {

    private BoomJobClient jobClient;

    private Map<String, JobProcessor> jobProcessorCache = new ConcurrentHashMap<>();

    private Set<String> jobs = new ConcurrentHashSet<>();

    private ApplicationConfig application;

    private RegistryConfig registry;

    private Map<Class<? extends Job>, ServiceConfig<JobProcessor>> serviceCache = new ConcurrentHashMap<>();

    private RegisterService registerService;

    public JobPool(BoomJobClient jobClient) {
        this.jobClient = jobClient;
        this.application = jobClient.applicationConfig();
        this.registry = jobClient.registerConfig();
        this.registerService = jobClient.registerService();
    }

    public JobProcessor getJobProcessor(Class<? extends Job> jobClass) {
        String jobId = getJobId(jobClass);
        return jobProcessorCache.get(jobId);
    }

    public void registerJob(Job job) {
        String jobId = getJobId(job.getClass());
        JobProcessor jobProcessor = new SimpleJobProcessor(job, jobId, jobClient);
        jobProcessorCache.put(jobId, jobProcessor);
        jobs.add(job.getClass().getCanonicalName());
        if (serviceCache.get(job.getClass()) != null)
            return;
        ServiceConfig<JobProcessor> service = new ServiceConfig<>();
        service.setApplication(application);
        service.setRegistry(registry);
        service.setProtocol(jobClient.protocolConfig());
        service.setInterface(JobProcessor.class);
        service.setGroup(jobId);
        service.setRef(jobProcessor);

        service.export();
        serviceCache.put(job.getClass(), service);
    }

    @Override
    public void start() {
        AppInfo appInfo = new AppInfo();
        appInfo.setAuthor(jobClient.author());
        appInfo.setAppName(jobClient.appName());
        appInfo.setJobs(new HashSet<>(jobs));
        registerService.registerAppInfo(appInfo);
    }

    @Override
    public void shutdown() {
        serviceCache.values().forEach(ServiceConfig::unexport);
        serviceCache.clear();
        jobProcessorCache.clear();
    }

    private String getJobId(Class<? extends Job> jobClass){
        return NameKit.getJobId(jobClient.appName(), jobClient.author(), jobClass.getCanonicalName());
    }
}
