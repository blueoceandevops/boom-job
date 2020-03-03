package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobKey {
    private Long jobId;
    private Long appId;
    private Long userId;
    private String user;
    private String appName;
    private String jobClassName;
}
