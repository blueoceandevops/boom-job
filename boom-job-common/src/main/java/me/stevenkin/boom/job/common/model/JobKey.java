package me.stevenkin.boom.job.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobKey {
    private Long jobId;
    private String author;
    private String appName;
    private String version;
    private String jobClassName;
}
