package me.stevenkin.boom.job.scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "boom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
public class BoomJobConfig {
    private DubboConfig dubbo;
    private ZkConfig zk;
    private QuartzConfig quartz;

    @Bean
    public ZkClient zkClient(){
        return new ZkClient(zk.getHosts(), zk.getNamespace());
    }
}
