package me.stevenkin.boom.job.test.cluster;

import com.alibaba.dubbo.config.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class TestClient {
    private TestConfig config;

    private ReferenceConfig<TestService> refer = new ReferenceConfig<>();
    private TestService testService;

    public TestClient(TestConfig config) {
        this.config = config;
        this.refer.setApplication(config.getApplication());
        this.refer.setRegistry(config.getRegistry());
        this.refer.setInterface(TestService.class);
        testService = this.refer.get();
    }

    public void test() {
        log.info(testService.hello("world"));
    }

    public void after() {
        refer.destroy();
    }

    public static void main(String[] args) {
        TestConfig config = new TestConfig();
        TestClient test = new TestClient(config);
        test.test();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
