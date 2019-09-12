package me.stevenkin.boom.job.data.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobScheduleDao {

    @Update("update job_info set schedulerId = #{schedulerId}, status = 0, updateTime = now() where id = #{jobId} and status = 1")
    Integer schedulerJob(@Param("jobId") Long jobId, @Param("schedulerId") String schedulerId);

    @Update("update job_info set status = 2, updateTime = now() where id = #{jobId} and status = 1 or status = 2")
    Integer triggerJob(Long jobId);

    @Update("update job_info set status = 3, updateTime = now() where id = #{jobId} and status = 0 or status = 2")
    Integer pauseJob(Long jobId);

    @Update("update job_info set status = 0, updateTime = now() where id = #{jobId} and status = 3")
    Integer resumeJob(Long jobId);

    @Update("update job_info set status = 1, updateTime = now() where id = #{jobId} and status = 3 or status = 0")
    Integer offlineJob(Long jobId);

    @Update("update job_info set status = -1, updateTime = now() where id = #{jobId} and status = 1")
    Integer disableJob(Long jobId);

}
