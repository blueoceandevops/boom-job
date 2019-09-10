package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInstanceShardDto implements Serializable {
    private static final long serialVersionUID = 890116025166420317L;

    private String jobId;
    private String jobType;
    private Long jobInstanceId;
    private Long jobShardId;
    private Long jobShardIndex;
    private String jobShardParam;
    private Long jobShardCount;
}
