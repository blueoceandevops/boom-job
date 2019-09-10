package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.po.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDetail {
    private Job job;
    private App app;
    private JobKey jobKey;
    private JobConfig jobConfig;
}
