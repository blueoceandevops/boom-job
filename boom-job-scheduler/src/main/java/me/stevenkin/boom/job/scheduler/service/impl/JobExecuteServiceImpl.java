package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.*;
import me.stevenkin.boom.job.common.enums.JobInstanceShardStatus;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.po.JobInstance;
import me.stevenkin.boom.job.common.po.JobInstanceShard;
import me.stevenkin.boom.job.common.po.JobKey;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.data.dao.JobInfoDao;
import me.stevenkin.boom.job.data.dao.JobInstanceDao;
import me.stevenkin.boom.job.data.dao.JobInstanceShardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class JobExecuteServiceImpl implements JobExecuteService {
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private JobInstanceDao jobInstanceDao;
    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;

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
        JobKey jobKey = jobInfoDao.selectJobKeyById(jobInstance.getJobId());
        dto.setJobKey(NameKit.getJobId(jobKey.getAppName(), jobKey.getAuthor(), jobKey.getJobClassName()));
        dto.setJobParam(jobInstance.getJobParam());
        dto.setJobShardCount(jobInstance.getShardCount());
        dto.setJobShardIndex(shard.getIndex());
        dto.setJobShardParam(shard.getParam());
        return dto;
    }

    @Override
    public List<Long> fetchMoreShardIds(Long jobInstance) {
        return jobInstanceShardDao.selectNewByJobInstanceIdIs(jobInstance).stream().map(JobInstanceShard::getId).collect(Collectors.toList());
    }

    @Override
    public Boolean checkJobInstanceIsFinish(Long jobInstance) {
        List<JobInstanceShard> shards = jobInstanceShardDao.selectByJobInstanceId(jobInstance);
        long count = shards.stream().filter(s -> s.getStatus() != 0).count();
        boolean isFinished = jobInstanceDao.selectById(jobInstance).getShardCount() == count;
        if (isFinished) {
            boolean isFailed = shards.stream().anyMatch(s -> s.getStatus() == 2 || s.getStatus() == 3);
            if (isFailed) {
                jobInstanceDao.updateJobInstanceStatus(jobInstance, 0, 2);
            }else {
                jobInstanceDao.updateJobInstanceStatus(jobInstance, 0, 1);
            }
        }
        return isFinished;
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
        //TODO send to log system
    }
}
