package me.stevenkin.boom.job.scheduler.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.JobDetail;
import me.stevenkin.boom.job.common.dto.JobFireRequest;
import me.stevenkin.boom.job.common.enums.JobInstanceShardStatus;
import me.stevenkin.boom.job.common.enums.JobInstanceStatus;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.po.JobInstance;
import me.stevenkin.boom.job.common.po.JobInstanceShard;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.storage.dao.JobInstanceDao;
import me.stevenkin.boom.job.storage.dao.JobInstanceShardDao;
import me.stevenkin.boom.job.storage.dao.JobScheduleDao;
import me.stevenkin.boom.job.scheduler.SchedulerContext;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JobExecutor {
    @Autowired
    private JobScheduleDao jobScheduleDao;
    @Autowired
    private JobInstanceDao jobInstanceDao;
    @Autowired
    private JobInstanceShardDao jobInstanceShardDao;
    @Autowired
    private SchedulerContext schedulerContext;

    @Transactional(rollbackFor = Exception.class)
    public void execute(JobDetail jobDetail, JobProcessor jobProcessor, JobExecutionContext context) {
        Integer n;
        Long jobId = jobDetail.getJob().getId();
        n = jobScheduleDao.triggerJob(jobId);
        if (n < 1) {
            throw new RuntimeException("job" + jobId + " trigger failed");
        }
        boolean allowConcurrent = jobDetail.getJobConfig().isAllowConcurrent();
        JobInstance jobInstance = new JobInstance();
        jobInstance.setJobId(jobId);
        jobInstance.setJobParam(jobDetail.getJobConfig().getJobParam());
        jobInstance.setStatus(JobInstanceStatus.RUNNING.getCode());
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
            throw new RuntimeException("job" + jobId + " insert job instance failed");
        }
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
        request.setJobInstanceId(jobInstance.getId());
        request.setJobShardIds(jobShardIds);
        request.setSchedulerId(schedulerContext.getSchedulerId());
        jobProcessor.fireJob(request);
    }

}
