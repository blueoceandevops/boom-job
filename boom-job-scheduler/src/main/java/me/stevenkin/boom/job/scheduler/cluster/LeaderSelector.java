package me.stevenkin.boom.job.scheduler.cluster;

import me.stevenkin.boom.job.common.exception.ZkException;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.scheduler.SchedulerContext;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.leader.Participant;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LeaderSelector implements InitializingBean, DisposableBean{
    private final static String LEADER = "/cluster/leader";

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private SchedulerContext schedulerContext;

    @Autowired
    private NodeFailedProcessor processor;

    private org.apache.curator.framework.recipes.leader.LeaderSelector selector;

    private CountDownLatch latch;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void destroy() throws Exception {
        processor.shutdown();
        scheduler.shutdown();
        if (latch != null)
            latch.countDown();
        zkClient.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        selector = new org.apache.curator.framework.recipes.leader.LeaderSelector(zkClient.getClient(), LEADER, new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                latch = new CountDownLatch(1);
                zkClient.update(LEADER, schedulerContext.getSchedulerId());
                processor.start();
                scheduler.scheduleAtFixedRate(() -> {
                    if (!Objects.equals(schedulerContext.getSchedulerId(), getLeader())) {
                        processor.shutdown();
                        scheduler.shutdown();
                        selector.requeue();
                        latch.countDown();
                    }
                }, 1, 5, TimeUnit.SECONDS);
                latch.await();
            }
        });
        selector.setId(schedulerContext.getSchedulerId());
        selector.start();
    }

    private String getLeader() {
        try {
            Participant p = selector.getLeader();
            if (p != null){
                return p.getId();
            }
        } catch (Exception e) {
            throw new ZkException(e);
        }
        return null;
    }
}
