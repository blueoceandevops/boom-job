package me.stevenkin.boom.job.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(SchedulerApplication.class, args);
        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
    }

}
