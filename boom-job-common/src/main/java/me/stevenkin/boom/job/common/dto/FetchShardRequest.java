package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchShardRequest implements Serializable {
    private static final long serialVersionUID = 8717193281500936488L;
    private Long jobInstanceShardId;
    private String clientId;
}
