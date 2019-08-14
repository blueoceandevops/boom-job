package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.job.JobProcessor;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.processor.annotation.BoomJob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JobPool implements Lifecycle {

    private BoomJobClient jobClient;

    private Map<String, JobProcessor> jobProcessorCache = new ConcurrentHashMap<>();

    private ApplicationConfig application = new ApplicationConfig();

    private RegistryConfig registry = new RegistryConfig();

    private Map<Class<? extends Job>, ServiceConfig<JobProcessor>> serviceCache = new ConcurrentHashMap<>();

    //TODO job register service

    public JobPool(BoomJobClient jobClient) {
        this.jobClient = jobClient;
    }

    public JobProcessor getJobProcessor(Class<? extends Job> jobClass) {
        String jobId = getJobId(jobClass);
        return jobProcessorCache.get(jobId);
    }

    public void registerJob(Job job) {
        String jobId = getJobId(job.getClass());
        String jobVersion = job.getClass().getAnnotation(BoomJob.class).version();
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
        //TODO report job info
    }

    @Override
    public void start() {
        this.application.setName(jobClient.appName());
        this.registry.setAddress(jobClient.zkHosts());
        this.registry.setProtocol("zookeeper");
        //TODO start job register service
    }

    @Override
    public void shutdown() {
        serviceCache.values().forEach(ServiceConfig::unexport);
        //TODO shutdown job register service
        serviceCache.clear();
        jobProcessorCache.clear();
    }

    private String getJobId(Class<? extends Job> jobClass){
        return Joiner.on(".").join(
                new String[]{jobClient.author(), jobClient.appName(), jobClass.getCanonicalName()});
    }
}
