package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.JobInfo;
import me.stevenkin.boom.job.common.job.JobProcessor;
import me.stevenkin.boom.job.common.job.RegisterService;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.processor.annotation.BoomJob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JobPool implements Lifecycle {

    private BoomJobClient jobClient;

    private Map<String, JobProcessor> jobProcessorCache = new ConcurrentHashMap<>();

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
        String jobName = job.getClass().getAnnotation(BoomJob.class).name();
        String jobVersion = job.getClass().getAnnotation(BoomJob.class).version();
        String jobDescription = job.getClass().getAnnotation(BoomJob.class).description();
        JobProcessor jobProcessor = new SimpleJobProcessor(job, jobId, jobVersion, jobClient.executor());
        jobProcessorCache.put(jobId, jobProcessor);
        if (serviceCache.get(job.getClass()) != null)
            return;
        ServiceConfig<JobProcessor> service = new ServiceConfig<>();
        service.setApplication(application);
        service.setRegistry(registry);
        service.setInterface(JobProcessor.class);
        service.setRef(jobProcessor);

        service.export();
        serviceCache.put(job.getClass(), service);

        registerService.registerJobInfo(new JobInfo(
                jobClient.appName(), jobClient.author(), job.getClass().getCanonicalName(), jobName, jobVersion, jobDescription));
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        serviceCache.values().forEach(ServiceConfig::unexport);
        serviceCache.clear();
        jobProcessorCache.clear();
    }

    private String getJobId(Class<? extends Job> jobClass){
        return NameKit.getJobId(jobClient.author(), jobClient.appName(), jobClass.getCanonicalName());
    }
}
