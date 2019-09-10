package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobContext {
    private String jobKey;
    private Long jobInstanceId;
    private Long jobShardId;
    private Long jobShardIndex;
    private String jobShardParam;
    private String jobParam;
    private Long jobShardCount;
}
