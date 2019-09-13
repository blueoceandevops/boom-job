package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.dto.JobResult;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobShardExecuteLog {
    private String jobKey;
    private Long jobInstanceId;
    private Long jobShardId;
    private String clientId;
    private Integer jobResult;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long executeTime;
    private Throwable exception;
}
