package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.*;
import me.stevenkin.boom.job.common.enums.JobInstanceShardStatus;
import me.stevenkin.boom.job.common.enums.JobInstanceStatus;
import me.stevenkin.boom.job.common.exception.ZkException;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.po.*;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.zk.model.JobInstanceNode;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.storage.dao.JobInfoDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceShardDao;
import me.stevenkin.boom.job.storage.dao.JobShardExecuteLogDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class JobExecuteServiceImpl implements JobExecuteService {
    private static final String JOB_INSTANCE_PATH = "/job_instance";
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private JobInstanceDao jobInstanceDao;
    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;
    @Autowired
    private JobShardExecuteLogDao jobShardExecuteLogDao;
    @Autowired
    private ZkClient zkClient;

    @Override
    @Transactional
    public JobInstanceShardDto fetchOneShard(FetchShardRequest request) {
        Integer n = jobInstanceShardDao.lockJobInstanceShard(request.getJobInstanceShardId(), request.getClientId());
        JobInstanceShard shard = jobInstanceShardDao.selectById(request.getJobInstanceShardId());
        if (n < 1) {
            if (shard.getStatus().equals(JobInstanceShardStatus.NEW.getCode()) && shard.getPullCount() >= shard.getMaxShardPullCount())
                jobInstanceShardDao.finishFailedShard(request.getJobInstanceShardId());
            return null;
        }
        JobInstanceShardDto dto = new JobInstanceShardDto();
        JobInstance jobInstance = jobInstanceDao.selectById(shard.getJobInstanceId());
        dto.setJobShardId(shard.getId());
        dto.setJobInstanceId(shard.getJobInstanceId());
        dto.setJobId(jobInstance.getJobId());
        JobKey jobKey = jobInfoDao.selectJobKeyById(jobInstance.getJobId());
        dto.setUserId(jobKey.getUserId());
        dto.setAppId(jobKey.getAppId());
        dto.setJobKey(NameKit.getJobKey(jobKey.getAppName(), jobKey.getUser(), jobKey.getJobClassName()));
        dto.setJobParam(jobInstance.getJobParam());
        dto.setJobShardCount(jobInstance.getShardCount());
        dto.setJobShardIndex(shard.getIndex());
        dto.setJobShardParam(shard.getParam());
        return dto;
    }

    @Override
    public List<Long> fetchMoreShardIds(Long jobInstance) {
        return jobInstanceShardDao.selectNewByJobInstanceId(jobInstance).stream().map(JobInstanceShard::getId).collect(Collectors.toList());
    }

    @Override
    public Boolean checkJobInstanceIsFinish(Long jobInstance) {
        List<JobInstanceShard> shards = jobInstanceShardDao.selectByJobInstanceId(jobInstance);
        long count = shards.stream().filter(s -> s.getStatus() != 0).count();
        JobInstance jobInstance1 = jobInstanceDao.selectById(jobInstance);
        if (JobInstanceStatus.fromCode(jobInstance1.getStatus()) != JobInstanceStatus.RUNNING)
            return true;
        boolean isFinished = jobInstance1.getShardCount() == count;
        if (isFinished) {
            boolean isFailed = shards.stream().anyMatch(s -> s.getStatus() == 2 || s.getStatus() == 3);
            int status;
            if (isFailed) {
                status = 2;
            }else {
                status = 1;
            }
            int n = jobInstanceDao.updateJobInstanceStatus(jobInstance, 0, status);
            if (n > 0) {
                String data = null;
                try {
                    data = new String(zkClient.get(PathKit.format(JOB_INSTANCE_PATH, jobInstance1.getJobId(), jobInstance)));
                } catch (ZkException e) {
                    log.warn("node is not exist, the job runtime instance node is removed", e);
                }
                if (!StringUtils.isBlank(data)) {
                    JobInstanceNode node = JSON.parseObject(data, JobInstanceNode.class);
                    JobInstanceNode node1 = new JobInstanceNode();
                    node1.setStatus(status);
                    node1.setStartTime(node.getStartTime());
                    node1.setExpectedEndTime(node.getExpectedEndTime());
                    try {
                        zkClient.update(PathKit.format(JOB_INSTANCE_PATH, jobInstance1.getJobId(), jobInstance), node1);
                    } catch (Exception e) {
                        //ignore this exception
                    }
                }
            }
        }
        return isFinished;
    }

    @Override
    public List<JobPlanRuntime> getJobPlanRuntime(Long jobInstanceId) {
        return null;
    }

    @Override
    public void reportJobExecResult(JobExecReport jobExecReport) {
        JobResult result = jobExecReport.getJobResult();
        switch (result.getCode()) {
            case 0:
                jobInstanceShardDao.finishSuccessShard(jobExecReport.getJobShardId());
                break;
            case 1:
                jobInstanceShardDao.finishFailedShard(jobExecReport.getJobShardId());
                break;
            case 2:
                jobInstanceShardDao.unlockJobInstanceShard(jobExecReport.getJobShardId());
                break;
            default:
                throw new IllegalStateException();
        }
        //send to log system
        JobShardExecuteLog log = new JobShardExecuteLog();
        BeanUtils.copyProperties(jobExecReport, log);
        log.setJobResult(jobExecReport.getJobResult().getCode());
        jobShardExecuteLogDao.insert(log);
    }
}
