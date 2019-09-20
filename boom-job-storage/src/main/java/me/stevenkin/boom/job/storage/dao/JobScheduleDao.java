package me.stevenkin.boom.job.storage.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobScheduleDao {

    @Update("update job_info set schedulerId = #{schedulerId}, status = 0, updateTime = now() where id = #{jobId} and status = 1")
    Integer onlineJob(@Param("jobId") Long jobId, @Param("schedulerId") String schedulerId);

    @Update("update job_info set status = 3, updateTime = now() where id = #{jobId} and status = 0")
    Integer pauseJob(Long jobId);

    @Update("update job_info set status = 0, updateTime = now() where id = #{jobId} and status = 3")
    Integer resumeJob(Long jobId);

    @Update("update job_info set status = 1, updateTime = now() where id = #{jobId} and (status = 3 or status = 0)")
    Integer offlineJob(Long jobId);

    @Update("update job_info set status = -1, updateTime = now() where id = #{jobId} and status = 1")
    Integer disableJob(Long jobId);

    @Update("update job_info set status = 1, updateTime = now() where id = #{jobId} and status = -1")
    Integer enableJob(Long jobId);

}
