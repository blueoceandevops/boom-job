package me.stevenkin.boom.job.scheduler.cluster;

import lombok.Setter;
import me.stevenkin.boom.job.common.exception.ZkException;
import me.stevenkin.boom.job.common.support.Lifecycle;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LeaderSelector extends Lifecycle {
    private final static String LEADER = "/leader";
    @Autowired
    private ZkClient zkClient;
    @Setter
    private String schedulerId;
    @Autowired
    private NodeFailedProcessor processor;

    private org.apache.curator.framework.recipes.leader.LeaderSelector selector;

    private volatile CountDownLatch latch;

    private volatile ScheduledExecutorService scheduler;

    private volatile boolean isLeader;

    private volatile boolean isPaused;

    private Lock lock = new ReentrantLock();

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

    @Override
    public void doStart() throws Exception {
        selector = new org.apache.curator.framework.recipes.leader.LeaderSelector(zkClient.getClient(), LEADER, new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                try {
                    lock.lock();
                    if (isLeader) {
                        return ;
                    }
                    if (isPaused) {
                        return ;
                    }
                    latch = new CountDownLatch(1);
                    zkClient.update(LEADER, schedulerId);
                    processor.start();
                    scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.scheduleAtFixedRate(() -> {
                        if (!Objects.equals(schedulerId, getLeader())) {
                            try {
                                lock.lock();
                                if (!isLeader)
                                    return ;
                                processor.pause();
                                if (scheduler != null) {
                                    scheduler.shutdown();
                                    scheduler = null;
                                }
                                releaseLeader();
                                try {
                                    selector.requeue();
                                }catch (Exception e) {
                                    //ignore exception
                                }
                                isLeader = false;
                            }catch (Exception e) {
                                throw new RuntimeException(e);
                            }finally {
                                lock.unlock();
                            }
                        }
                    }, 1, 5, TimeUnit.SECONDS);
                    isLeader = true;
                }finally {
                    lock.unlock();
                }
                latch.await();
            }
        });
        selector.setId(schedulerId);
        selector.start();
        isLeader = false;
        isPaused = false;
    }

    @Override
    public void doPause() throws Exception {
        try {
            lock.lock();
            if (isPaused) {
                return ;
            }
            if (isLeader) {
                releaseLeader();
            }
            selector.close();
            isPaused = true;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void doResume() throws Exception {
        doStart();
    }

    @Override
    public void doShutdown() throws Exception {

    }

    public void releaseLeader() {
        if (latch != null) {
            latch.countDown();
            latch = null;
        }
    }
}
