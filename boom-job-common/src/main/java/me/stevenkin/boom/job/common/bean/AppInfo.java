package me.stevenkin.boom.job.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppInfo implements Serializable{
    private static final long serialVersionUID = 43727524604536152L;

    private String appName;
    private String author;
    private String version;
}
