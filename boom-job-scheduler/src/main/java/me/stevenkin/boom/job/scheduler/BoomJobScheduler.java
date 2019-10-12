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
    private static final String CMD_SCHEDULER = "/command/scheduler";

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

    private String schedulerId;

    @Override
    public void doStart() throws Exception {
        // scan dubbo provider and export
        dubboProviderScanner.start();
        // get scheduler id use provider url
        schedulerId = dubboProviderScanner.getSchedulerId();
        // listen client cluster
        clientCluster.start();
        // lister scheduler cluster
        schedulerCluster.start();
        // start leader selector
        leaderSelector.setSchedulerId(schedulerId);
        leaderSelector.start();
        //start job manager
        jobManager.start();
        // start command processor
        initCommandProcessor();
    }

    private void initCommandProcessor() throws Exception{
        commandProcessor = new CommandProcessor();
        commandProcessor.setComponent(this);
        commandProcessor.setZkClient(zkClient);
        commandProcessor.setCommandPath(CMD_SCHEDULER);
        commandProcessor.setId(schedulerId);
        commandProcessor.start();
    }

    @Override
    public void doPause() throws Exception {
        dubboProviderScanner.pause();
        clientCluster.pause();
        schedulerCluster.pause();
        leaderSelector.pause();
        jobManager.pause();
        commandProcessor.pause();
    }

    @Override
    public void doResume() throws Exception {
        dubboProviderScanner.resume();
        clientCluster.resume();
        schedulerCluster.resume();
        leaderSelector.resume();
        jobManager.resume();
        commandProcessor.resume();
    }

    @Override
    public void doShutdown() throws Exception {
        dubboProviderScanner.shutdown();
        clientCluster.shutdown();
        schedulerCluster.shutdown();
        leaderSelector.shutdown();
        jobManager.shutdown();
        commandProcessor.shutdown();
    }
}
