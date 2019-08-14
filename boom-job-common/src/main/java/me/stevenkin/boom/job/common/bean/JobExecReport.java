package me.stevenkin.boom.job.common.bean;

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

    private String jobId;
    private String jobType;
    private String jobInstanceId;
    private String jobShardId;
    private String processorId;
    private JobResult jobResult;
    private Instant startTime;
    private Instant endTime;
    private Throwable exception;

}
