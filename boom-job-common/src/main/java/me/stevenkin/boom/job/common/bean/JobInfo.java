package me.stevenkin.boom.job.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInfo implements Serializable{
    private static final long serialVersionUID = 7852332761131470264L;

    private String appName;
    private String author;
    private String jobClassName;
    private String jobName;
    private String jobVersion;
    private String jobDescription;
}
