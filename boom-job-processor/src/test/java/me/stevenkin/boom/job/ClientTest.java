package me.stevenkin.boom.job;

import com.alibaba.dubbo.config.*;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.JobFireRequest;
import me.stevenkin.boom.job.common.bean.JobFireResponse;
import me.stevenkin.boom.job.common.job.JobExecReportService;
import me.stevenkin.boom.job.common.job.JobProcessor;
import me.stevenkin.boom.job.common.job.RegisterService;
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

        ServiceConfig<JobExecReportService> service2 = new ServiceConfig<>();
        service2.setApplication(application);
        service2.setRegistry(registry);
        service2.setProtocol(protocol);
        service2.setInterface(JobExecReportService.class);
        service2.setRef(new JobReportServiceTest());
        service2.export();

        BoomJobClient jobClient = new SimpleBoomJobClient(
                "test", "stevenkin","0.0.1", "wjg",
                "192.168.99.100:2181", "boom", 1);
        jobClient.start();
        jobClient.registerJob(new TestJob());

        ReferenceConfig<JobProcessor> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(JobProcessor.class);
        reference.setGroup("stevenkin_test_0.0.1_me.stevenkin.boom.job.TestJob");
        JobProcessor jobProcessor = reference.get();
        JobFireResponse response = jobProcessor.fireJob(new JobFireRequest(
                "stevenkin_test_0.0.1_me.stevenkin.boom.job.TestJob",
                "default",
                "0",
                "0",
                0L,
                "",
                1L,
                "127.0.0.1"));
        log.info(response.toString());
    }

}
