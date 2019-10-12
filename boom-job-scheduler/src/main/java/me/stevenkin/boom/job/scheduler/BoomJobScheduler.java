package me.stevenkin.boom.job.scheduler;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.CommandProcessor;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.scheduler.cluster.ClientCluster;
import me.stevenkin.boom.job.scheduler.cluster.LeaderSelector;
import me.stevenkin.boom.job.scheduler.cluster.SchedulerCluster;
import me.stevenkin.boom.job.scheduler.core.JobManager;
import me.stevenkin.boom.job.scheduler.dubbo.DubboProviderScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BoomJobScheduler extends Lifecycle {
    private static final String CMD_SCHEDULER = "command/scheduler";

    @Autowired
    private ClientCluster clientCluster;
    @Autowired
    private SchedulerCluster schedulerCluster;
    @Autowired
    private LeaderSelector leaderSelector;
    @Autowired
    private JobManager jobManager;
    @Autowired
    private DubboProviderScanner dubboProviderScanner;
    @Autowired
    private ZkClient zkClient;

    private CommandProcessor commandProcessor;

    @Override
    public void doStart() throws Exception {
        dubboProviderScanner.start();
        clientCluster.start();
        schedulerCluster.start();
        leaderSelector.start();
        jobManager.start();
        initCommandProcessor();
    }

    private void initCommandProcessor() throws Exception{
        commandProcessor = new CommandProcessor();
        commandProcessor.setComponent(this);
        commandProcessor.setZkClient(zkClient);
        commandProcessor.setCommandPath(CMD_SCHEDULER);
        commandProcessor.setId(dubboProviderScanner.getSchedulerId());
        commandProcessor.start();
    }

    @Override
    public void doPause() throws Exception {

    }

    @Override
    public void doResume() throws Exception {

    }

    @Override
    public void doShutdown() throws Exception {

    }
}
