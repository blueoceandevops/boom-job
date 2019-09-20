package me.stevenkin.boom.job.storage.dao;

import me.stevenkin.boom.job.common.po.App;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.JobKey;

import java.util.List;

public interface JobInfoDao {

    Job selectById(Long id);

    JobKey selectJobKeyById(Long id);

    List<Job> selectJobsByAppId(Long appId);

    List<Long> selectOnlineJobBySchedulerId(String schedulerId);

    Job selectJobByJobKey(String author, String appName, String jobClass);

    Integer countByAppId(Long appId);

    Integer count();

    Integer insert(Job job);

    Integer delete(Long id);

    Integer update(Job job);

    Integer updateByStatus(Integer status, Job job);

}
