package me.stevenkin.boom.job.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobContext {
    private String jobId;
    private String jobType;
    private String jobInstanceId;
    private String jobShardId;
    private String jobShardIndex;
    private String jobShardParam;
    private Integer jobShardCount;
    private String schedulerId;
}
