package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobFireRequest implements Serializable{
    private static final long serialVersionUID = -4153657694380103021L;
    private String jobKey;
    private String jobClass;
    private Long jobId;
    private Long jobInstanceId;
    private List<Long> jobShardIds;
    private String schedulerId;
    private List<String> blacklist;
}
