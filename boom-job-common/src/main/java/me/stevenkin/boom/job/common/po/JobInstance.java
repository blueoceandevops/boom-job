package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInstance {
    private Long id;
    private Long jobId;
    private Integer status;
    private String jobParam;
    private Integer shardCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
