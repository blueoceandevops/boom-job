package me.stevenkin.boom.job.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.model.App;
import me.stevenkin.boom.job.common.model.Job;
import me.stevenkin.boom.job.common.model.JobConfig;
import me.stevenkin.boom.job.common.model.JobKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDetail {
    private Job job;
    private App app;
    private JobKey jobKey;
    private JobConfig jobConfig;
}
