package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecReport implements Serializable{
    private static final long serialVersionUID = 7508627524604536152L;

    private String jobKey;
    private Long jobInstanceId;
    private Long jobShardId;
    private String clientId;
    private JobResult jobResult;
    private Long executeTime;
    private Throwable exception;

}