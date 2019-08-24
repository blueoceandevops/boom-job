package me.stevenkin.boom.job.scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzConfig {
    private Integer scheduleThreadCount;
    private String scheduleThreadPoolClass;
}
