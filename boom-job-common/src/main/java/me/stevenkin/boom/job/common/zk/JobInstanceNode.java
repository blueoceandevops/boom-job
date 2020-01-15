package me.stevenkin.boom.job.common.zk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInstanceNode {
    private Long jobId;
    private Long jobInstanceId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;
}
