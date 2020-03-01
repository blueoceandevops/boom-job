package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.enums.JobInstanceStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPlanRuntime {
    private Long id;
    private Long planJobInstanceId;
    private Long jobId;
    private Long jobInstanceId;
    private JobInstanceStatus status;

    public JobPlanRuntime(Long planJobInstanceId, Long jobId, Long jobInstanceId, JobInstanceStatus status) {
        this.planJobInstanceId = planJobInstanceId;
        this.jobId = jobId;
        this.jobInstanceId = jobInstanceId;
        this.status = status;
    }
}
