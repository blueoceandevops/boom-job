package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.AppInfo;
import me.stevenkin.boom.job.common.service.AppRegisterService;
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

    private AppRegisterService appRegisterService;

    public JobPool(BoomJobClient jobClient) {
        this.jobClient = jobClient;
        this.appRegisterService = jobClient.registerService();
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
        Set<String> jobs1 = new HashSet<>(jobs);
        AppInfo appInfo = new AppInfo();
        appInfo.setAuthor(jobClient.author());
        appInfo.setAppName(jobClient.appName());
        appInfo.setJobs(jobs1);
        appRegisterService.registerAppInfo(appInfo);
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
