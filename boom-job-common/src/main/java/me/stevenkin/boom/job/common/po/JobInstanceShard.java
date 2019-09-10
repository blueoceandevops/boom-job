package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInstanceShard {
    private Long id;
    private Long jobInstanceId;
    private Integer index;
    private String param;
    private String clientId;
    private Integer status;
    private Integer pullCount;
    private Integer maxShardPullCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
