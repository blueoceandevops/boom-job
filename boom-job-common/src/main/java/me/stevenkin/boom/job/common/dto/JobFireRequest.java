package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobFireRequest implements Serializable{
    private static final long serialVersionUID = -4153657694380103021L;
    private String jobKey;
    private Long jobInstanceId;
    private Long jobInstanceShardId;
    private String schedulerId;
}