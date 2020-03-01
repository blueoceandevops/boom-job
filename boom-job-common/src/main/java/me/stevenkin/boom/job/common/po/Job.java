package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.enums.JobType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    private Long id;
    private String jobClass;
    private JobType type;//job type, simple default
    private Integer status;
    private String schedulerId;
    private Long appId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
