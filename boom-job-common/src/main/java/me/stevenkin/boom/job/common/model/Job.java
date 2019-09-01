package me.stevenkin.boom.job.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    private Long id;
    private String jobClass;
    private Integer type;
    private Integer status;
    private String cron;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer repeatCount;
    private Integer repeatInterval;
    private boolean isRepeatForever;
    private String desc;
    private Long appId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
