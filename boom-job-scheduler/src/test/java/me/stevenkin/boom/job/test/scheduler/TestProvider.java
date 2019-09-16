package me.stevenkin.boom.job.test.scheduler;

import com.alibaba.dubbo.config.ServiceConfig;

import java.io.IOException;

public class TestProvider {
    private ServiceConfig<TestService> service = new ServiceConfig<>();

    public TestProvider(TestConfig testConfig) {
        service.setApplication(testConfig.getApplication());
        service.setRegistry(testConfig.getRegistry());
        service.setProtocol(testConfig.getProtocol());
        service.setCluster("test");
        service.setInterface(TestService.class);
        service.setRef(new TestService() {
            @Override
            public String hello(String str) {
                return "hello " + str;
            }
        });
    }

    public void start() {
        service.export();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        service.unexport();
    }

    public static void main(String[] args) {
        TestConfig config = new TestConfig();
        new TestProvider(config).start();
    }
}
