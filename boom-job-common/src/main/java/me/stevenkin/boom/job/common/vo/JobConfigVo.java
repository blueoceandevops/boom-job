package me.stevenkin.boom.job.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobConfigVo implements Serializable {
    private static final long serialVersionUID = -775260523450903720L;
    private Long jobId;
    private String cron;
    private String desc;
    private boolean onlineNow;
    private boolean misfire;
    private boolean allowConcurrent;
    private String jobParam;
    private Integer shardCount;
    private String shardParams;
    private Integer maxShardPullCount;
    private Long timeout;
}
