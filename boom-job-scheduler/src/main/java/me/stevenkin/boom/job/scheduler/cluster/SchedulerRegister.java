package me.stevenkin.boom.job.scheduler.cluster;

import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.scheduler.SchedulerContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SchedulerRegister implements InitializingBean, DisposableBean {
    private static final String ZKPREFIX = "scheduler";
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private SchedulerContext schedulerContext;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduler.scheduleAtFixedRate(() -> {
            String schedulerNode = PathKit.format(ZKPREFIX, schedulerContext.getSchedulerId());
            if (!zkClient.checkExists(schedulerNode)) {
                zkClient.createEphemeral(schedulerNode);
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        scheduler.shutdown();
        zkClient.shutdown();
    }
}
