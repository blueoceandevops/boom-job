package me.stevenkin.boom.job.scheduler.listener;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.scheduler.BoomJobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SchedulerStartListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private BoomJobScheduler scheduler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("scheduler is starting...");
        try {
            scheduler.start();
            log.info("scheduler start success...");
        } catch (Exception e) {
            log.error("scheduler start failed", e);
        }
    }
}
