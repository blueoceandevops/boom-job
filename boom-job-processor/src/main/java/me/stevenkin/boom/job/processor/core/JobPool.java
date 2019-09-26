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
public class JobPool extends Lifecycle {

    private BoomJobClient jobClient;

    private Set<String> jobs = new ConcurrentHashSet<>();

    private Map<String, Job> jobCache = new ConcurrentHashMap<>();

    private RegisterService registerService;

    public JobPool(BoomJobClient jobClient) {
        this.jobClient = jobClient;
        this.registerService = jobClient.registerService();
    }

    public Job getJob(String jobClass) {
        return jobCache.get(jobClass);
    }

    public void registerJob(Job job) {
        jobCache.putIfAbsent(job.getClass().getCanonicalName(), job);
        jobs.add(job.getClass().getCanonicalName());
    }

    @Override
    public void doStart() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setAuthor(jobClient.author());
        appInfo.setAppName(jobClient.appName());
        appInfo.setJobs(new HashSet<>(jobs));
        registerService.registerAppInfo(appInfo);
    }

    @Override
    public void doPause() throws Exception {

    }

    @Override
    public void doResume() throws Exception {

    }

    @Override
    public void doShutdown() throws Exception {
        jobs.clear();
        jobCache.clear();
    }
}
