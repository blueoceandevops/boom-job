package me.stevenkin.boom.job.scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZkConfig {
    private String namespace = "boom";
    private String hosts;
}
