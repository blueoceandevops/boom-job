package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.*;
import me.stevenkin.boom.job.common.enums.JobFireResult;
import me.stevenkin.boom.job.common.service.ClientProcessor;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.ShardExecuteService;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SimpleClientProcessor extends Lifecycle implements ClientProcessor {
    private BoomJobClient jobClient;

    private JobPool jobPool;

    private ServiceConfig<ClientProcessor> service;

    private ExecutorService executor;

    private JobExecuteService jobExecuteService;

    private String clientId;

    private String appKey;

    private CountDownLatch latch;

    public SimpleClientProcessor(BoomJobClient jobClient) {
        this.jobClient = jobClient;
        this.jobPool = jobClient.jobPool();
        this.executor = jobClient.executor();
        this.jobExecuteService = jobClient.jobExecuteService();
        this.appKey = jobClient.appKey();

        this.service.setApplication(jobClient.applicationConfig());
        this.service.setRegistry(jobClient.registerConfig());
        this.service.setProtocol(jobClient.protocolConfig());
        this.service.setInterface(ClientProcessor.class);
        this.service.setGroup(this.appKey);
        this.service.setRef(this);

        latch = new CountDownLatch(1);
    }

    @Override
    public JobFireResponse fireJob(JobFireRequest request) throws Exception {
        latch.await();
        List<String> clients = new ArrayList<>();
        clients.add(clientId);
        Job job = jobPool.getJob(request.getJobClass());
        if (job == null) {
            return new JobFireResponse(JobFireResult.FIRE_FAILED, clients);
        }
        String jobId = jobId(request.getJobClass());
        Long jobInstanceId = request.getJobInstanceId();
        List<Long> jobShardIds = request.getJobShardIds();
        Queue<Long> shardIds = new LinkedList<>();
        shardIds.addAll(jobShardIds);
        executor.submit(() -> {
            log.info("service {} is fired", jobId);
            while (!jobExecuteService.checkJobInstanceIsFinish(jobInstanceId)) {
                Long id;
                while ((id = shardIds.poll()) != null) {
                    JobInstanceShardDto jobInstanceShardDto = jobExecuteService.fetchOneShard(new FetchShardRequest(id, jobClient.clientId()));
                    if (jobInstanceShardDto != null) {
                        jobExecuteService.reportJobExecResult(executeJobShard(job, jobId, jobInstanceShardDto));
                    }
                }
                shardIds.addAll(jobExecuteService.fetchMoreShardIds(jobInstanceId));
            }
        });
        return new JobFireResponse(JobFireResult.FIRE_SUCCESS, clients);
    }

    private JobExecReport executeJobShard(Job job, String jobId, JobInstanceShardDto dto) {
        JobResult result;
        Throwable error = null;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime;
        try {
            result = job.execute(buildJobContext(dto));
            endTime = LocalDateTime.now();
        } catch (Throwable throwable) {
            result = JobResult.FAIL;
            error = throwable;
            endTime = LocalDateTime.now();
            log.error("some error happen when service {} is executing", jobId, throwable);
        }
        Long execTime = Duration.between(startTime, endTime).toMillis();
        JobExecReport jobExecReport = new JobExecReport();
        jobExecReport.setJobKey(jobId);
        jobExecReport.setJobInstanceId(dto.getJobInstanceId());
        jobExecReport.setJobShardId(dto.getJobShardId());
        jobExecReport.setClientId(jobClient.clientId());
        jobExecReport.setStartTime(startTime);
        jobExecReport.setEndTime(endTime);
        jobExecReport.setExecuteTime(execTime);
        jobExecReport.setJobResult(result);
        jobExecReport.setException(error);
        return jobExecReport;
    }

    private JobContext buildJobContext(JobInstanceShardDto dto) {
        JobContext jobContext = new JobContext();
        BeanUtils.copyProperties(dto, jobContext);
        return jobContext;
    }

    private String jobId(String jobClass) {
        return appKey + "_" + jobClass;
    }

    @Override
    public void doStart() throws Exception {
        service.export();
        this.clientId = getClientId(service);
        latch.countDown();
    }

    public String getClientId() {
        return this.clientId;
    }

    private String getClientId(ServiceConfig<ClientProcessor> service) {
        URL url = service.toUrl();
        String startTime = url.getParameter("timestamp");
        if (StringUtils.isEmpty(startTime)) {
            throw new RuntimeException();
        }
        Integer.parseInt(startTime);
        return url.getHost() + "_" + url.getPort() + "_" + startTime;
    }

    @Override
    public void doPause() throws Exception {
        //TODO disable service
    }

    @Override
    public void doResume() throws Exception {
        //TODO enable service
    }

    @Override
    public void doShutdown() throws Exception {
        service.unexport();
    }
}
