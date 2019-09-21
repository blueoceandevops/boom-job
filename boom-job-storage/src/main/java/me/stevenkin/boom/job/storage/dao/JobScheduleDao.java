package me.stevenkin.boom.job.storage.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobScheduleDao {

    @Update("update job_info set scheduler_id = #{schedulerId}, status = 0, updateTime = now() where id = #{jobId} and status = 1")
    Integer onlineJob(@Param("jobId") Long jobId, @Param("schedulerId") String schedulerId);

    @Update("update job_info set scheduler_id = #{newSchedulerId}, updateTime = now() where id = #{jobId} and scheduler_id = #{schedulerId} and (status = 0 or status = 3)")
    Integer failoverJob(@Param("jobId") Long jobId, @Param("schedulerId") String schedulerId, @Param("newSchedulerId") String newSchedulerId);

    @Update("update job_info set status = 3, updateTime = now() where id = #{jobId} and status = 0 and scheduler_id = #{schedulerId}")
    Integer pauseJob(Long jobId, @Param("schedulerId") String schedulerId);

    @Update("update job_info set status = 0, updateTime = now() where id = #{jobId} and status = 3 and scheduler_id = #{schedulerId}")
    Integer resumeJob(Long jobId, @Param("schedulerId") String schedulerId);

    @Update("update job_info set status = 1, updateTime = now() where id = #{jobId} and (status = 3 or status = 0) and scheduler_id = #{schedulerId}")
    Integer offlineJob(Long jobId, @Param("schedulerId") String schedulerId);

    @Update("update job_info set status = -1, updateTime = now() where id = #{jobId} and status = 1")
    Integer disableJob(Long jobId);

    @Update("update job_info set status = 1, updateTime = now() where id = #{jobId} and status = -1")
    Integer enableJob(Long jobId);

}
