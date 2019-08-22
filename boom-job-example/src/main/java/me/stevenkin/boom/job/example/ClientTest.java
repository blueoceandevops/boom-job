package me.stevenkin.boom.job.example;

import com.alibaba.dubbo.config.*;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.JobFireRequest;
import me.stevenkin.boom.job.common.bean.JobFireResponse;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.common.service.ShardExecuteService;
import me.stevenkin.boom.job.processor.core.BoomJobClient;
import me.stevenkin.boom.job.processor.core.SimpleBoomJobClient;

@Slf4j
public class ClientTest {

    public static void main(String[] args) {
        log.info("start test......");
        ApplicationConfig application = new ApplicationConfig();
        RegistryConfig registry = new RegistryConfig();
        application.setName("test");
        registry.setAddress("192.168.99.100:2181");
        registry.setProtocol("zookeeper");
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(-1);
        protocol.setThreads(200);

        ServiceConfig<RegisterService> service1 = new ServiceConfig<>();
        service1.setApplication(application);
        service1.setRegistry(registry);
        service1.setProtocol(protocol);
        service1.setInterface(RegisterService.class);
        service1.setRef(new RegisterServiceTest());
        service1.export();

        ServiceConfig<JobExecuteService> service2 = new ServiceConfig<>();
        service2.setApplication(application);
        service2.setRegistry(registry);
        service2.setProtocol(protocol);
        service2.setInterface(JobExecuteService.class);
        service2.setRef(new JobExecuteServiceTest());
        service2.export();

        ServiceConfig<ShardExecuteService> service3 = new ServiceConfig<>();
        service3.setApplication(application);
        service3.setRegistry(registry);
        service3.setProtocol(protocol);
        service3.setInterface(ShardExecuteService.class);
        service3.setRef(new ShardExecuteServiceTest());
        service3.export();


        BoomJobClient jobClient = new SimpleBoomJobClient(
                "test", "stevenkin","0.0.1", "wjg",
                "192.168.99.100:2181", "boom", 1);
        jobClient.start();
        jobClient.registerJob(new TestJob());

        ReferenceConfig<JobProcessor> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(JobProcessor.class);
        reference.setGroup("stevenkin_test_0.0.1_me.stevenkin.boom.job.example.TestJob");
        JobProcessor jobProcessor = reference.get();
        JobFireResponse response = jobProcessor.fireJob(new JobFireRequest(
                "stevenkin_test_0.0.1_me.stevenkin.boom.job.example.TestJob",
                "default",
                0L,
                3L,
                "127.0.0.1"));
        log.info(response.toString());
    }

}
