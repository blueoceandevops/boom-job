package me.stevenkin.boom.job.scheduler.cluster;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.SimpleZkQueue;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.common.zk.ZkElement;
import me.stevenkin.boom.job.common.zk.ZkQueue;
import me.stevenkin.boom.job.scheduler.service.FailoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class NodeFailedProcessor implements Lifecycle{
    private final static String CLIENT_PATH = "failover/client";
    private final static String SCHEDULER_PATH = "failover/scheduler";

    @Autowired
    private FailoverService failoverService;

    @Autowired
    private ZkClient zkClient;

    private ZkQueue failedClientQueue;

    private ZkQueue failedSchedulerQueue;

    private volatile boolean started = false;

    private ExecutorService service;

    @Override
    public synchronized void start() {
        try {
            if (!started) {
                started = true;
                failedClientQueue = new SimpleZkQueue(zkClient, CLIENT_PATH);
                failedSchedulerQueue = new SimpleZkQueue(zkClient, SCHEDULER_PATH);
                service = Executors.newFixedThreadPool(2);
                service.submit(() -> {
                    while (started) {
                        ZkElement element = failedClientQueue.take();
                        log.info("start process failed client {} ", element.getNode());
                        try {
                            failoverService.processClientFailed(element.getNode());
                            zkClient.delete(PathKit.format(CLIENT_PATH, element.getNode()));
                            log.info("finish process failed client {} ", element.getNode());
                        }catch (Exception e) {
                            log.error("process client {} failover happen error", element.getNode(), e);
                            failedClientQueue.put(element);
                        }
                    }
                });
                service.submit(() -> {
                    while (started) {
                        ZkElement element = failedSchedulerQueue.take();
                        log.info("start process failed scheduler {} ", element.getNode());
                        try {
                            failoverService.processSchedulerFailed(element.getNode());
                            zkClient.delete(PathKit.format(SCHEDULER_PATH, element.getNode()));
                            log.info("finish process failed scheduler {} ", element.getNode());
                        }catch (Exception e) {
                            log.error("process scheduler {} failover happen error", element.getNode(), e);
                            failedSchedulerQueue.put(element);
                        }
                    }
                });
            }
        }catch (Exception e) {
            log.error("NodeFailedProcessor start failed", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public synchronized void shutdown() {
        if (!started)
            return;
        try {
            failedClientQueue.close();
            failedSchedulerQueue.close();
            if (service != null) {
                service.shutdownNow();
            }
            started = false;
        }catch (Exception e) {
            log.error("NodeFailedProcessor shutdown failed", e);
            throw new RuntimeException(e);
        }
    }
}
