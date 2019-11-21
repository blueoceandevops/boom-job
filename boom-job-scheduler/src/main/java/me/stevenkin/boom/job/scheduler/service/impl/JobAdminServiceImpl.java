package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobConfig;
import me.stevenkin.boom.job.common.service.JobAdminService;
import me.stevenkin.boom.job.common.service.JobSchedulerService;
import me.stevenkin.boom.job.common.vo.JobConfigVo;
import me.stevenkin.boom.job.storage.dao.JobConfigDao;
import me.stevenkin.boom.job.storage.dao.JobInfoDao;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Service
public class JobAdminServiceImpl implements JobAdminService {
    @Reference
    private JobSchedulerService jobSchedulerService;
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private JobConfigDao jobConfigDao;

    @Override
    public List<Job> listJobPagingByApp(Long appId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return jobInfoDao.selectJobsByAppId(appId);
    }

    @Override
    public JobConfig getJobConfigByJobId(Long jobId) {
        return jobConfigDao.selectByJobId(jobId);
    }

    @Override
    public Boolean saveJob(JobConfigVo jobConfigVo) {
        JobConfig jobConfig = new JobConfig();
        BeanUtils.copyProperties(jobConfigVo, jobConfig);
        int n = jobConfigDao.insertOrUpdate(jobConfig);
        if (n == 1) {
            if (jobConfig.isOnlineNow())
                jobSchedulerService.onlineJob(jobConfig.getJobId());
            return Boolean.TRUE;
        }else if (n == 2) {
            jobSchedulerService.reloadJob(jobConfig.getJobId());
            return Boolean.TRUE;
        }else if (n == 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean deleteJobAndConfig(Long jobId) {
        Boolean offlineSuccess = jobSchedulerService.offlineJob(jobId);
        if (!offlineSuccess) {
            return Boolean.FALSE;
        }
        return jobInfoDao.delete(jobId) > 0 && jobConfigDao.deleteByJobId(jobId) > 0;
    }

    @Override
    public Boolean onlineJob(Long jobId) {
        return jobSchedulerService.onlineJob(jobId);
    }

    @Override
    public Boolean triggerJob(Long jobId) {
        return jobSchedulerService.triggerJob(jobId) != null;
    }

    @Override
    public Boolean pauseJob(Long jobId) {
        return jobSchedulerService.pauseJob(jobId);
    }

    @Override
    public Boolean resumeJob(Long jobId) {
        return jobSchedulerService.resumeJob(jobId);
    }

    @Override
    public Boolean offlineJob(Long jobId) {
        return jobSchedulerService.offlineJob(jobId);
    }
}
