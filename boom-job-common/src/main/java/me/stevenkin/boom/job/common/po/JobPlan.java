package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPlan {
    private Long id;
    private Long jobId;
    private Long fromJob;
    private Long toJob;
    private Boolean ignoreFailed;
    private Boolean ignoreTimeout;
    private Boolean ignoreTerminate;
}
