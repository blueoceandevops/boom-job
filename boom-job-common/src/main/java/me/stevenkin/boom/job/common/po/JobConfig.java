package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobConfig {
    private Long id;
    private Long jobId;
    private String cron;
    private String desc;
    private boolean onlineNow;
    private boolean misfire;
    private boolean allowConcurrent;
    private String jobParam;
    private Integer shardCount;
    private String shardParams;
    private Integer maxShardPullCount;
    private Long timeout;// unit is second
}
