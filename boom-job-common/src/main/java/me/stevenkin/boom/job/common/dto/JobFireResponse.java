package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.enums.JobFireResult;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobFireResponse implements Serializable {
    private static final long serialVersionUID = -5130877794594938052L;

    private JobFireResult jobFireResult;
    private String clientId;
}
