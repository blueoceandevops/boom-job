package me.stevenkin.boom.job.scheduler.config;

import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@ConfigurationProperties(prefix = "boom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
public class BoomJobConfig {
    private final static String CLIENT = "client";
    private final static String SCHEDULER = "scheduler";
    private final static String JOB = "job";
    private final static String CLIENT_FAILOVER = "failover/client";
    private final static String SCHEDULER_FAILOVER = "failover/scheduler";

    private DubboConfig dubbo;
    private ZkConfig zk;
    private QuartzConfig quartz;

    @Bean
    public ZkClient zkClient(){
        ZkClient zkClient = new ZkClient(zk.getHosts(), zk.getNamespace());
        zkClient.mkdirs(CLIENT);
        zkClient.mkdirs(SCHEDULER);
        zkClient.mkdirs(JOB);
        zkClient.mkdirs(CLIENT_FAILOVER);
        zkClient.mkdirs(SCHEDULER_FAILOVER);
        return zkClient;
    }

    @Bean
    public PageHelper pageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties p = new Properties();
        p.setProperty("offsetAsPageNum", "true");
        p.setProperty("rowBoundsWithCount", "true");
        p.setProperty("reasonable", "true");
        pageHelper.setProperties(p);
        return pageHelper;
    }
}
