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
import me.stevenkin.boom.job.common.exception.ZkException;
import me.stevenkin.boom.job.common.kit.Holder;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.po.JobInstance;
import me.stevenkin.boom.job.common.po.JobInstanceShard;
import me.stevenkin.boom.job.common.service.ClientProcessor;
import me.stevenkin.boom.job.common.zk.JobInstanceNode;
import me.stevenkin.boom.job.common.zk.JobInstanceNodeListener;
import me.stevenkin.boom.job.common.zk.NodeListener;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.storage.dao.BlacklistDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceShardDao;
import me.stevenkin.boom.job.storage.dao.JobScheduleDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.Stat;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Component
@Slf4j
public class JobExecutor {
    private static final String JOB_INSTANCE_PATH = "/job_instance";
    @Autowired
    private JobInstanceDao jobInstanceDao;
    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;
    @Autowired
    private JobScheduleDao jobScheduleDao;
    @Autowired
    private BlacklistDao blacklistDao;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Setter
    private String schedulerId;
    @Getter
    private CountDownLatch latch = new CountDownLatch(1);

    @Transactional(rollbackFor = Exception.class)
    public Long execute(JobDetail jobDetail, ClientProcessor clientProcessor) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("happen error", e);
        }
        Integer n;
        Long jobId = jobDetail.getJob().getId();
        //when a job is scheduled by multiple servers, ensure only one can trigger success
        n = jobScheduleDao.triggerJob(jobId, schedulerId);
        if (n != 1) {
            log.error("job" + jobId + " trigger failed");
            return null;
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
            return null;
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
        request.setJobKey(NameKit.getJobId(jobDetail.getJobKey().getAppName(), jobDetail.getJobKey().getAuthor(), jobDetail.getJobKey().getJobClassName()));
        request.setJobId(jobId);
        request.setJobInstanceId(jobInstance.getId());
        request.setJobShardIds(jobShardIds);
        request.setSchedulerId(schedulerId);
        request.setBlacklist(blacklistDao.selectByJobId(jobId));

        zkClient.createIfNotExists(PathKit.format(JOB_INSTANCE_PATH, jobId));
        JobInstanceNode node = new JobInstanceNode(jobId, jobInstanceId, jobInstance.getStatus(),
                jobInstance.getStartTime(),
                jobInstance.getExpectedEndTime());
        zkClient.create(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), JSON.toJSON(node));

        taskExecutor.submit(() -> callProcessorAndWait(jobId, jobInstanceId, jobInstance, jobDetail, request, clientProcessor));
        return jobInstanceId;
    }

    private void callProcessorAndWait(Long jobId, Long jobInstanceId, JobInstance jobInstance, JobDetail jobDetail, JobFireRequest request, ClientProcessor clientProcessor) {
        CountDownLatch latch1 = new CountDownLatch(1);
        zkClient.registerNodeCacheListener(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), new JobInstanceNodeListener()
                .add(o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.SUCCESS || status == JobInstanceStatus.FAILED;
                }, o -> latch1.countDown())
                .add(o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.RUNNING;
                }, o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    jobInstanceDao.updateJobInstanceStatus(node.getJobInstanceId(), JobInstanceStatus.NEW.getCode(), JobInstanceStatus.RUNNING.getCode());
                })
                .add(o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.TIMEOUT;
                }, o -> {
                })
                .add(o -> {
                    JobInstanceNode node = (JobInstanceNode) o;
                    JobInstanceStatus status = JobInstanceStatus.fromCode(node.getStatus());
                    return status == JobInstanceStatus.TERMINATE;
                }, o -> latch1.countDown())
        );

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
        if (endTime - startTime > jobDetail.getJobConfig().getTimeout()) {
            int n = jobInstanceDao.updateJobInstanceStatus(jobInstanceId, JobInstanceStatus.RUNNING.getCode(), JobInstanceStatus.TIMEOUT.getCode());
            // 更新job实例状态成功，说明已超时，就争取终止正在运行的执行器
            if (n > 0) {
                zkClient.update(PathKit.format(JOB_INSTANCE_PATH, jobId, jobInstanceId), new JobInstanceNode(jobId, jobInstanceId, JobInstanceStatus.TIMEOUT.getCode(),
                        jobInstance.getStartTime(),
                        jobInstance.getExpectedEndTime()));
            }
        }
        // TODO remove zk job instance node
    }

}
