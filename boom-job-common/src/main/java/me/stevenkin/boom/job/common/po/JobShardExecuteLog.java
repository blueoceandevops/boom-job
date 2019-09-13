package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobShardExecuteLog {
    private String jobKey;
    private Long jobInstanceId;
    private Long jobShardId;
    private String clientId;
    private Integer jobResult;
    private Long executeTime;
    private String exception;
}
