package me.stevenkin.boom.job.storage.dao;

import me.stevenkin.boom.job.common.po.JobInstanceShard;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface JobInstanceShardDao {

    @Insert("insert into job_instance_shard" +
            "(job_instance_id, index, param, status, " +
            "pull_count, max_shard_pull_count, start_time, create_time, update_time)" +
            "values (#{jobInstanceId}, #{index}, #{param}, #{clientId}" +
            "#{status}, #{pullCount}, #{maxShardPullCount}, #{startTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertJobInstanceShard(JobInstanceShard jobInstanceShard);

    @Update("update job_instance_shard set status = 1, client_id = #{clientId}, pull_count = pull_count + 1, update_time = now()" +
            "where id = #{id} and status = 0 and pull_count < max_shard_pull_count")
    Integer lockJobInstanceShard(@Param("id") Long id, @Param("clientId") String clientId);

    @Update("update job_instance_shard set status = 0, client_id = null, update_time = now() where id = #{id} and status = 1")
    Integer unlockJobInstanceShard(@Param("id") Long id);

    @Update("update job_instance_shard set status = 2, end_time = now(), update_time = now() where id = #{id} and status = 1")
    Integer finishSuccessShard(@Param("id") Long id);

    @Update("update job_instance_shard set status = 3, end_time = now(), update_time = now() where id = #{id} and status = 1")
    Integer finishFailedShard(@Param("id") Long id);

    @Select("select * from job_instance_shard where job_instance_id = #{jobInstanceId}")
    List<JobInstanceShard> selectByJobInstanceId(@Param("jobInstanceId") Long jobInstanceId);

    @Select("select * from job_instance_shard where job_instance_id = #{jobInstanceId} and status = 0")
    List<JobInstanceShard> selectNewByJobInstanceId(@Param("jobInstanceId") Long jobInstanceId);

    @Select("select * from job_instance_shard where client_id = #{clientId} and status = 1")
    List<JobInstanceShard> selectRunningByClientId(@Param("clientId") Long clientId);

    @Select("select * from job_instance_shard")
    List<JobInstanceShard> selectAll();

    @Select("select * from job_instance_shard where (status = 2 or status = 3) and create_time < #{time}")
    List<JobInstanceShard> selectFinishedBefore(@Param("time") LocalDateTime time);

    @Select("select * from job_instance_shard where id = #{id}")
    JobInstanceShard selectById(@Param("id") Long id);

    @Delete("delete from job_instance_shard where id = #{id}")
    Integer deleteById(@Param("id") Long id);
}
