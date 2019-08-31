package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.model.App;
import me.stevenkin.boom.job.common.model.Job;

import java.util.List;

public interface JobInfoDao {

    Job selectById(Long id);

    List<Job> selectJobsByApp(App app);

    Job selectJobByAppAndJobClass(App app, String jobClass);

    Integer countByApp(App app);

    Integer count();

    Integer insert(Job job);

    Integer delete(Job job);

    Integer update(Job job);

    Integer updateWhereStatusIs(Integer status, Job job);

}
