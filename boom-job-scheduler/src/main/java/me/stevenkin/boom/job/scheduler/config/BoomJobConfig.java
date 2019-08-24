package me.stevenkin.boom.job.scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "boom")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoomJobConfig {
    private DubboConfig dubbo;
    private ZkConfig zk;
    private QuartzConfig quartz;
}
