package me.stevenkin.boom.job.scheduler.listener;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.scheduler.BoomJobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SchedulerShutdownListener implements ApplicationListener<ContextClosedEvent> {
    @Autowired
    private BoomJobScheduler scheduler;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        log.info("scheduler is stopping...");
        try {
            scheduler.shutdown();
            log.info("scheduler stop success...");
        } catch (Exception e) {
            log.error("scheduler stop failed", e);
        }
    }
}
