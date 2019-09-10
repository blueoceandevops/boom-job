package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchShardResponse implements Serializable{
    private static final long serialVersionUID = 2810186199122784143L;

    private JobInstanceShardDto jobInstanceShard;
    private Boolean instanceIsFinal;
}
