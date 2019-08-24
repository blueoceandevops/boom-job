package me.stevenkin.boom.job.scheduler.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import lombok.Data;
import me.stevenkin.boom.job.scheduler.config.BoomJobConfig;
import me.stevenkin.boom.job.scheduler.config.DubboConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Data
public class DubboConfigHolder {

    @Autowired
    private BoomJobConfig boomJobConfig;

    private ApplicationConfig applicationConfig;

    private RegistryConfig registryConfig;

    private ProtocolConfig protocolConfig;

    private ConsumerConfig consumerConfig;

    @PostConstruct
    public void init() {
        DubboConfig dubboConfig = boomJobConfig.getDubbo();

        applicationConfig = new ApplicationConfig();
        applicationConfig.setName(dubboConfig.getApplicationName());
        applicationConfig.setVersion(dubboConfig.getApplicationVersion());

        registryConfig = new RegistryConfig();
        registryConfig.setAddress(dubboConfig.getRegisterAddress());
        registryConfig.setPort(dubboConfig.getRegisterPort());
        registryConfig.setProtocol(dubboConfig.getRegisterProtocol());

        protocolConfig = new ProtocolConfig();
        protocolConfig.setName(dubboConfig.getProtocolName());
        protocolConfig.setPort(dubboConfig.getProtocolPort());
        protocolConfig.setThreads(dubboConfig.getProtocolThreads());

        consumerConfig = new ConsumerConfig();
        consumerConfig.setTimeout(dubboConfig.getTimeout());
        consumerConfig.setRetries(dubboConfig.getRetries());
    }

}
