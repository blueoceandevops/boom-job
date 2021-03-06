package me.stevenkin.boom.job.common.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class App {
    private Long id;
    private String appName;
    private String appSecret;
    private String desc;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
