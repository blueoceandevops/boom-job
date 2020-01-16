package me.stevenkin.boom.job.processor.core;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.*;
import me.stevenkin.boom.job.common.dubbo.Configuration;
import me.stevenkin.boom.job.common.dubbo.Node;
import me.stevenkin.boom.job.common.enums.JobFireResult;
import me.stevenkin.boom.job.common.enums.JobInstanceStatus;
import me.stevenkin.boom.job.common.exception.ZkException;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.kit.URLKit;
import me.stevenkin.boom.job.common.service.ClientProcessor;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.JobInstanceNode;
import me.stevenkin.boom.job.common.zk.JobInstanceNodeListener;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class SimpleClientProcessor extends Lifecycle implements ClientProcessor {
    private static final String JOB_INSTANCE_PATH = "/job_instance";

    private BoomJobClient jobClient;

    private JobPool jobPool;

    private ZkClient zkClient;

    private ServiceConfig<ClientProcessor> service;

    private ExecutorService executor;

    private JobExecuteService jobExecuteService;

    private RegistryService registryService;

    private String clientId;

    private Node clientNode;

    private String appKey;

    private CountDownLatch latch;

    private URL serviceUrl;

    private URL configUrl;

    private ConcurrentMap<Long, ConcurrentMap<Long, Future<?>>> map;

    public SimpleClientProcessor(BoomJobClient jobClient) {
        this.jobClient = jobClient;
        this.jobPool = jobClient.jobPool();
        this.zkClient = jobClient.zkClient();
        this.executor = jobClient.executor();
        this.jobExecuteService = jobClient.jobExecuteService();
        this.registryService = jobClient.registryService();
        this.appKey = jobClient.appKey();

        this.service.setApplication(jobClient.applicationConfig());
        this.service.setRegistry(jobClient.registerConfig());
        this.service.setProtocol(jobClient.protocolConfig());
        this.service.setInterface(ClientProcessor.class);
        this.service.setGroup(this.appKey);
        this.service.setRef(this);

        latch = new CountDownLatch(1);

        map = new ConcurrentHashMap<>();
    }

    @Override
    public JobFireResponse fireJob(JobFireRequest request) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        List<String> clients = new ArrayList<>();
        clients.add(clientId);
        if (request.getBlacklist() != null && request.getBlacklist().contains(clientNode.getAddress()))
            return new JobFireResponse(JobFireResult.FIRE_FAILED, clients);
        Job job = jobPool.getJob(request.getJobClass());
        if (job == null) {
            return new JobFireResponse(JobFireResult.FIRE_FAILED, clients);
        }
        String jobKey = jobKey(request.getJobClass());
        Long jobId = request.getJobId();
        Long jobInstanceId = request.getJobInstanceId();
        List<Long> jobShardIds = request.getJobShardIds();
        Queue<Long> shardIds = new LinkedList<>();
        shardIds.addAll(jobShardIds);

        zkClient.registerNodeCacheListener(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), new JobInstanceNodeListener().add(
                o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.SUCCESS || status == JobInstanceStatus.FAILED;
                }, o -> {

                }
        ).add(
                o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.RUNNING;
                }, o -> {

                }
        ).add(
                o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.TIMEOUT;
                }, o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    ConcurrentMap<Long, Future<?>> map1;
                    Future<?> future = null;
                    if ((map1 = map.get(node.getJobId())) != null && (future = map1.remove(node.getJobInstanceId())) != null) {
                        future.cancel(true);
                        zkClient.delete(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId));
                    }
                }
        ).add(
                o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.TERMINATE;
                }, o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    ConcurrentMap<Long, Future<?>> map1;
                    Future<?> future = null;
                    if ((map1 = map.get(node.getJobId())) != null && (future = map1.remove(node.getJobInstanceId())) != null) {
                        future.cancel(true);
                    }
                }
        ));

        Future<?> future = executor.submit(() -> {
            log.info("service {} is fired", jobKey);
            while (!jobExecuteService.checkJobInstanceIsFinish(jobInstanceId)) {
                Long id;
                while ((id = shardIds.poll()) != null) {
                    JobInstanceShardDto jobInstanceShardDto = jobExecuteService.fetchOneShard(new FetchShardRequest(id, jobClient.clientId()));
                    if (jobInstanceShardDto != null) {
                        jobExecuteService.reportJobExecResult(executeJobShard(job, jobKey, jobInstanceShardDto));
                    }
                }
                shardIds.addAll(jobExecuteService.fetchMoreShardIds(jobInstanceId));
            }
        });
        ConcurrentMap<Long, Future<?>> map1 = map.computeIfAbsent(jobId, id -> new ConcurrentHashMap<>());
        map1.putIfAbsent(jobInstanceId, future);
        String data = null;
        try {
            data = new String(zkClient.get(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId)));
        } catch (ZkException e) {
            log.warn("node is not exist, the job runtime instance node is removed", e);
        }
        if (!StringUtils.isBlank(data)) {
            JobInstanceNode node = JSON.parseObject(data, JobInstanceNode.class);
            node.setStatus(JobInstanceStatus.RUNNING.getCode());
            zkClient.update(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), node);
        }

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

    private String jobKey(String jobClass) {
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
        serviceUrl = service.toUrl();
        clientNode = URLKit.urlToNode(serviceUrl);
        return clientNode.toString();
    }

    @Override
    public void doPause() throws Exception {
        //disable service
        if (serviceUrl == null)
            throw new IllegalStateException();
        Configuration configuration = new Configuration();
        configuration.setService(serviceUrl.getServiceInterface());
        configuration.setAddress(serviceUrl.getAddress());
        configuration.setPort(serviceUrl.getPort());
        configuration.setEnabled(true);
        configUrl = configuration.toUrl();
        registryService.register(configUrl);
    }

    @Override
    public void doResume() throws Exception {
        //enable service
        if (configUrl == null)
            throw new IllegalStateException();
        registryService.unregister(configUrl);
        configUrl = null;
    }

    @Override
    public void doShutdown() throws Exception {
        service.unexport();
    }
}
