package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.po.JobInstance;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface JobInstanceDao {

    @Insert("insert into job_instance" +
            "(job_id, status, job_param, shard_count, " +
            "start_time, expected_end_time, create_time, update_time)" +
            "values (#{jobId}, #{status}, #{jobParam}, #{shardCount}, #{startTime}, #{expectedEndTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertJobInstance(JobInstance jobInstance);

    @Insert("insert into job_instance" +
            "(job_id, status, job_param, shard_count," +
            " start_time, expected_end_time, create_time, update_time)" +
            " SELECT #{jobId}, #{status}, #{jobParam}, #{shardCount}, #{startTime}, #{expectedEndTime}, #{createTime}, #{updateTime} " +
            "from dual where not exists(select id from job_instance where job_id = #{jobId} and status = 0)")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertJobInstanceOnlyAllowOneRunning(JobInstance jobInstance);

    @Update("update job_instance set status = #{status} where id = #{id} and status = #{expectStatus}")
    Integer updateJobInstanceStatus(@Param("id") Long id, @Param("expectStatus") Integer expectStatus, @Param("status") Integer status);

    @Select("select * from job_instance where id = #{id}")
    JobInstance selectById(@Param("id") Long id);

    @Select("select count(0) from job_instance where id = #{id}")
    Integer countFinalById(@Param("id") Long id);

    @Select("select * from job_instance where jobId = #{jobId}")
    List<JobInstance> selectByJobId(@Param("jobId") Long jobId);

    @Select("select * from job_instance")
    List<JobInstance> selectAll();

    @Select("select * from job_instance where (status = 1 or status = 2 or status = 3) and start_time < #{time}")
    List<JobInstance> selectFinishedBefore(@Param("time") LocalDateTime time);

    @Delete("delete from job_instance where id = #{id}")
    Integer deleteById(@Param("id") Long id);

}
