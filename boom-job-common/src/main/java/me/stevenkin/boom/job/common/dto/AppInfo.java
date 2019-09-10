package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppInfo implements Serializable{
    private static final long serialVersionUID = 43727524604536152L;
    private String appName;
    private String author;
    private Set<String> jobs;
}
