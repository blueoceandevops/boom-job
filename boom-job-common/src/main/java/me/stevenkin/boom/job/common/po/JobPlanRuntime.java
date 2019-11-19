package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPlanRuntime {
    private Long id;
    private Long planJobInstanceId;
    private Long jobInstanceId;
}
