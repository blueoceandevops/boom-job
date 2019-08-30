package me.stevenkin.boom.job.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobConfig {
    private Long id;
    private Long jobId;
    private boolean misfire;
    private String jobParam;
    private Integer shardCount;
    private String shardParams;
    private Integer maxShardPullCount;
    private Long timeout;// unit is second
}
