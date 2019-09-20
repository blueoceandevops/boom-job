package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.JobResult;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.po.JobInstanceShard;
import me.stevenkin.boom.job.common.po.JobKey;
import me.stevenkin.boom.job.common.po.JobShardExecuteLog;
import me.stevenkin.boom.job.common.service.JobSchedulerService;
import me.stevenkin.boom.job.scheduler.service.FailoverService;
import me.stevenkin.boom.job.storage.dao.JobInfoDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceShardDao;
import me.stevenkin.boom.job.storage.dao.JobShardExecuteLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FailoverServiceImpl implements FailoverService {
    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private JobInstanceDao jobInstanceDao;
    @Autowired
    private JobShardExecuteLogDao jobShardExecuteLogDao;
    @Reference
    private JobSchedulerService jobSchedulerService;

    @Override
    public void processClientFailed(String clientId) {
        List<JobInstanceShard> shards = jobInstanceShardDao.selectRunningByClientId(clientId);
        shards.forEach(s -> {
            jobInstanceShardDao.unlockJobInstanceShard(s.getId());
            //send to log system
            JobShardExecuteLog log = new JobShardExecuteLog();
            log.setJobResult(JobResult.DOWNTIME.getCode());
            log.setClientId(clientId);
            log.setJobInstanceId(s.getJobInstanceId());
            log.setJobShardId(s.getId());
            JobKey jobKey = jobInfoDao.selectJobKeyById(jobInstanceDao.selectById(s.getJobInstanceId()).getJobId());
            log.setJobKey(NameKit.getJobId(jobKey.getAppName(), jobKey.getAuthor(), jobKey.getJobClassName()));
            jobShardExecuteLogDao.insert(log);
        });
    }

    @Override
    public void processSchedulerFailed(String schedulerId) {
        List<Long> jobIds = jobInfoDao.selectOnlineJobBySchedulerId(schedulerId);
        jobIds.forEach(id -> {
            boolean success = jobSchedulerService.failoverJob(id, schedulerId);
            log.info("scheduler {}'s job {} failover {}", schedulerId, id, success ? "success" : "failed");
        });
    }
}
