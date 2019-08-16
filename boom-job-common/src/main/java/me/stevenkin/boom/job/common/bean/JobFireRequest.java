package me.stevenkin.boom.job.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobFireRequest implements Serializable{
    private static final long serialVersionUID = -4153657694380103021L;

    private String jobId;
    private String jobType;
    private String jobInstanceId;
    private String jobShardId;
    private Long jobShardIndex;
    private String jobShardParam;
    private Long jobShardCount;
    private String schedulerId;
}
