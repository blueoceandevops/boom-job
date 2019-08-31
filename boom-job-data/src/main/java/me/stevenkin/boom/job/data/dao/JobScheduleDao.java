package me.stevenkin.boom.job.data.dao;

import java.time.Instant;

public interface JobScheduleDao {

    Integer insertJobScheduleTurnover(Long jobId, String schedulerId, Instant instant);

}
