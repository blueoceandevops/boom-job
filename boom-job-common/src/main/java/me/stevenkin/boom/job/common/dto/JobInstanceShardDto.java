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

    private String jobKey;
    private Long userId;
    private Long appId;
    private Long jobId;
    private Long jobInstanceId;
    private Long jobShardId;
    private Integer jobShardIndex;
    private String jobShardParam;
    private String jobParam;
    private Integer jobShardCount;
}
