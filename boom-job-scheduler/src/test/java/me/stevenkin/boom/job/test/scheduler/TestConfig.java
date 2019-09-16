package me.stevenkin.boom.job.test.scheduler;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import lombok.Getter;

@Getter
public class TestConfig {
    private ApplicationConfig application;
    private RegistryConfig registry;
    private ProtocolConfig protocol;

    public TestConfig() {
        application = new ApplicationConfig();
        registry = new RegistryConfig();
        protocol = new ProtocolConfig();

        application.setName("test");
        registry.setAddress("127.0.0.1:2181");
        registry.setProtocol("zookeeper");
        protocol.setName("dubbo");
        protocol.setPort(-1);
        protocol.setThreads(200);
    }
}
