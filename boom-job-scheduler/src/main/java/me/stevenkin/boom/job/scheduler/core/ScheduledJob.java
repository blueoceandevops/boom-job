package me.stevenkin.boom.job.scheduler.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.model.Job;
import me.stevenkin.boom.job.common.support.Lifecycle;
import org.quartz.SchedulerFactory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledJob implements Lifecycle {
    private Long jobId;

    private SchedulerFactory schedulers;

    private CommandProcessor commandProcessor;

    public Boolean online() {
        return null;
    }

    public Boolean trigger() {
        return null;
    }

    public Boolean pause() {
        return null;
    }

    public Boolean resume() {
        return null;
    }

    public Boolean offline() {
        return null;
    }

    public Boolean reload(Job job) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
