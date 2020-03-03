package me.stevenkin.boom.job.scheduler.core;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.JobDetail;
import me.stevenkin.boom.job.common.dto.JobFireRequest;
import me.stevenkin.boom.job.common.enums.JobInstanceShardStatus;
import me.stevenkin.boom.job.common.enums.JobInstanceStatus;
import me.stevenkin.boom.job.common.enums.JobTriggerType;
import me.stevenkin.boom.job.common.enums.JobType;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.po.JobInstance;
import me.stevenkin.boom.job.common.po.JobInstanceShard;
import me.stevenkin.boom.job.common.po.JobPlanRuntime;
import me.stevenkin.boom.job.common.service.ClientProcessor;
import me.stevenkin.boom.job.common.support.ActionOnCondition;
import me.stevenkin.boom.job.common.support.Attachment;
import me.stevenkin.boom.job.common.zk.model.JobInstanceNode;
import me.stevenkin.boom.job.common.zk.JobInstanceNodeListener;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.storage.dao.*;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Component
@Slf4j
public class JobExecutor {
    private static final String JOB_INSTANCE_PATH = "/job_instance";

    private static final String RUNTIME_PATH = "runtime";
    @Autowired
    private JobInstanceDao jobInstanceDao;
    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;
    @Autowired
    private JobScheduleDao jobScheduleDao;
    @Autowired
    private JobPlanRuntimeDao jobPlanRuntimeDao;
    @Autowired
    private BlacklistDao blacklistDao;
    @Autowired
    private ZkClient zkClient;
    @Setter
    private String schedulerId;
    @Getter
    private CountDownLatch latch = new CountDownLatch(1);

    private ConcurrentMap<Long, NodeCache> nodeCacheMap = new ConcurrentHashMap<>();

    @Transactional(rollbackFor = Exception.class)
    public void execute(JobDetail jobDetail, ClientProcessor clientProcessor, JobTriggerType type, Attachment attach) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        Integer n;
        Long jobId = jobDetail.getJob().getId();
        JobType jobType = jobDetail.getJob().getType();
        //when a job is scheduled by multiple servers, ensure only one can trigger success
        n = jobScheduleDao.triggerJob(jobId, schedulerId);
        if (n != 1) {
            log.error("job" + jobId + " trigger failed");
            return;
        }
        boolean allowConcurrent = jobDetail.getJobConfig().isAllowConcurrent();
        JobInstance jobInstance = new JobInstance();
        jobInstance.setJobId(jobId);
        jobInstance.setJobParam(jobDetail.getJobConfig().getJobParam());
        jobInstance.setStatus(JobInstanceStatus.NEW.getCode());
        jobInstance.setShardCount(jobDetail.getJobConfig().getShardCount());
        jobInstance.setStartTime(LocalDateTime.now());
        jobInstance.setExpectedEndTime(jobInstance.getStartTime().plusSeconds(jobDetail.getJobConfig().getTimeout()));
        jobInstance.setCreateTime(jobInstance.getStartTime());
        jobInstance.setUpdateTime(jobInstance.getStartTime());
        if (allowConcurrent) {
            n = jobInstanceDao.insertJobInstance(jobInstance);
        }else {
            n = jobInstanceDao.insertJobInstanceOnlyAllowOneRunning(jobInstance);
        }
        if (n < 1) {
            log.error("job" + jobId + " insert job instance failed");
            return;
        }
        Long jobInstanceId = jobInstance.getId();
        String jobShardParam = jobDetail.getJobConfig().getShardParams();
        JSONArray jsonArray = JSON.parseArray(jobShardParam);
        List<Long> jobShardIds = new ArrayList<>();
        for (int i = 0; i < jobDetail.getJobConfig().getShardCount(); i++) {
            JobInstanceShard shard = new JobInstanceShard();
            shard.setJobInstanceId(jobInstance.getId());
            shard.setIndex(i);
            shard.setStatus(JobInstanceShardStatus.NEW.getCode());
            shard.setParam(i >= jsonArray.size() ? null : jsonArray.getObject(i, String.class));
            shard.setMaxShardPullCount(jobDetail.getJobConfig().getMaxShardPullCount());
            shard.setPullCount(0);
            shard.setStartTime(LocalDateTime.now());
            shard.setCreateTime(shard.getStartTime());
            shard.setUpdateTime(shard.getStartTime());
            jobInstanceShardDao.insertJobInstanceShard(shard);
            jobShardIds.add(shard.getId());
        }
        JobFireRequest request = new JobFireRequest();
        request.setJobKey(NameKit.getJobKey(jobDetail.getJobKey().getAppName(), jobDetail.getJobKey().getUser(), jobDetail.getJobKey().getJobClassName()));
        request.setJobId(jobId);
        request.setJobInstanceId(jobInstance.getId());
        request.setJobShardIds(jobShardIds);
        request.setSchedulerId(schedulerId);
        request.setBlacklist(blacklistDao.selectByJobId(jobId));

        zkClient.createIfNotExists(PathKit.format(JOB_INSTANCE_PATH, jobId));
        JobInstanceNode node = new JobInstanceNode(jobId, jobInstanceId, jobInstance.getStatus(),
                LocalDateTime.now(),
                jobInstance.getStartTime(),
                jobInstance.getExpectedEndTime());
        zkClient.create(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), JSON.toJSONString(node));
        if (jobType == JobType.PLAN) {
            List<JobPlanRuntime> runtimes = new ArrayList<>();
            zkClient.create(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId, RUNTIME_PATH), JSON.toJSONString(runtimes));
        }

        callProcessorAndWait(jobId, jobInstanceId, jobInstance, jobDetail, request, type, clientProcessor, attach);
    }

    private void callProcessorAndWait(Long jobId,
                                      Long jobInstanceId,
                                      JobInstance jobInstance,
                                      JobDetail jobDetail,
                                      JobFireRequest request,
                                      JobTriggerType triggerType,
                                      ClientProcessor clientProcessor,
                                      Attachment attach) {
        CountDownLatch latch1 = new CountDownLatch(1);
        NodeCache nodeCache = zkClient.registerNodeCacheListener(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), new JobInstanceNodeListener()
                .add(new ActionOnCondition<JobInstanceNode, JobInstanceNode>() {
                    @Override
                    public boolean test(JobInstanceNode jobInstanceNode) {
                        JobInstanceStatus status = JobInstanceStatus.fromCode(jobInstanceNode.getStatus());
                        return status == JobInstanceStatus.SUCCESS || status == JobInstanceStatus.FAILED;
                    }

                    @Override
                    public void action(JobInstanceNode jobInstanceNode) {
                        latch1.countDown();
                        jobExecRuntime(triggerType, jobInstanceNode, attach);
                        afterExecute(jobInstanceNode);
                    }
                }).add(new ActionOnCondition<JobInstanceNode, JobInstanceNode>() {
                    @Override
                    public boolean test(JobInstanceNode jobInstanceNode) {
                        JobInstanceStatus status = JobInstanceStatus.fromCode(jobInstanceNode.getStatus());
                        return status == JobInstanceStatus.RUNNING;
                    }

                    @Override
                    public void action(JobInstanceNode jobInstanceNode) {
                        jobInstanceDao.updateJobInstanceStatus(jobInstanceNode.getJobInstanceId(), JobInstanceStatus.NEW.getCode(), JobInstanceStatus.RUNNING.getCode());
                        jobExecRuntime(triggerType, jobInstanceNode, attach);
                    }
                }).add(new ActionOnCondition<JobInstanceNode, JobInstanceNode>() {
                    @Override
                    public boolean test(JobInstanceNode jobInstanceNode) {
                        JobInstanceStatus status = JobInstanceStatus.fromCode(jobInstanceNode.getStatus());
                        return status == JobInstanceStatus.TIMEOUT;
                    }

                    @Override
                    public void action(JobInstanceNode jobInstanceNode) {
                        jobExecRuntime(triggerType, jobInstanceNode, attach);
                        afterExecute(jobInstanceNode);
                    }
                }).add(new ActionOnCondition<JobInstanceNode, JobInstanceNode>() {
                    @Override
                    public boolean test(JobInstanceNode jobInstanceNode) {
                        JobInstanceStatus status = JobInstanceStatus.fromCode(jobInstanceNode.getStatus());
                        return status == JobInstanceStatus.TERMINATE;
                    }

                    @Override
                    public void action(JobInstanceNode jobInstanceNode) {
                        latch1.countDown();
                        jobExecRuntime(triggerType, jobInstanceNode, attach);
                        afterExecute(jobInstanceNode);
                    }
                })
        );
        nodeCacheMap.putIfAbsent(jobInstanceId, nodeCache);
        try {
            nodeCache.start();
        } catch (Exception e) {
            log.error("happen error", e);
            throw new RuntimeException(e);
        }

        try {
            clientProcessor.fireJob(request);
        } catch (RpcException e) {
            //TODO 告警
            //TODO 报RPC异常，有可能执行机全都挂了,就需要一个定时任务把超时的job instance节点置为超时状态
            //TODO 定时任务主要做检查一个正在运行状态的zk任务实例节点有没有超时，如果超时，再等待一段时间检查节点有没有被删除，如果没有就说明job调度机已宕机则删除节点
        }

        long startTime = System.currentTimeMillis();
        try {
            latch1.await(jobDetail.getJobConfig().getTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //ignore this exception
        }
        long endTime = System.currentTimeMillis();
        // 超时
        if (endTime - startTime >= jobDetail.getJobConfig().getTimeout()) {
            int n = jobInstanceDao.updateJobInstanceStatus(jobInstanceId, JobInstanceStatus.RUNNING.getCode(), JobInstanceStatus.TIMEOUT.getCode());
            // 更新job实例状态成功，说明已超时，就尝试终止正在运行的执行器
            if (n > 0) {
                zkClient.update(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), new JobInstanceNode(jobId, jobInstanceId, JobInstanceStatus.TIMEOUT.getCode(),
                        LocalDateTime.now(),
                        jobInstance.getStartTime(),
                        jobInstance.getExpectedEndTime()));
            }
        }
        // TODO 定时任务移除zk job instance节点
    }

    private void jobExecRuntime(JobTriggerType type, JobInstanceNode jobInstanceNode, Attachment attach) {
        switch (type) {
            case AUTO:
                break;
            case MANUAL:
                break;
            case PLAN:
                Optional<Object> optional = attach.get("planJobInstanceId");
                if (!optional.isPresent() || !(optional.get() instanceof Long)) {
                    log.error("must has planJobInstanceId");
                    return;
                }
                Long planJobInstanceId = (Long) optional.get();
                Long jobId = jobInstanceNode.getJobId();
                Long jobInstanceId = jobInstanceNode.getJobInstanceId();
                JobInstanceStatus jobInstanceStatus = JobInstanceStatus.fromCode(jobInstanceNode.getStatus());

                JobPlanRuntime runtime = jobPlanRuntimeDao.selectByPlanJobInstanceIdAndJobId(planJobInstanceId, jobId);
                if (runtime == null) {
                    jobPlanRuntimeDao.insertJobPlanRuntime(new JobPlanRuntime(
                        planJobInstanceId,
                        jobId,
                        jobInstanceId,
                        jobInstanceStatus
                    ));
                } else {
                    runtime.setStatus(jobInstanceStatus);
                    jobPlanRuntimeDao.updateJobPlanRuntime(runtime);
                }

                List<JobPlanRuntime> runtimes = jobPlanRuntimeDao.selectByPlanJobInstanceId(planJobInstanceId);
                zkClient.update(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId, RUNTIME_PATH), JSON.toJSONString(runtimes));
                break;
            default:
                log.error("no this job trigger type " + type.getMessage());
        }
    }

    private void afterExecute(JobInstanceNode jobInstanceNode) {
        NodeCache nodeCache1;
        if ((nodeCache1 = nodeCacheMap.get(jobInstanceNode.getJobInstanceId())) != null) {
            try {
                nodeCache1.close();
            } catch (IOException e) {
                log.error("happen error", e);
            }
        }
    }

}
