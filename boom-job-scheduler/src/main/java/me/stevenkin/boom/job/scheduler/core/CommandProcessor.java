package me.stevenkin.boom.job.scheduler.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stevenkin.boom.job.common.model.JobKey;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandProcessor implements Lifecycle {
    private JobKey jobKey;

    private ZkClient zkClient;

    private JobManager jobManager;

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
