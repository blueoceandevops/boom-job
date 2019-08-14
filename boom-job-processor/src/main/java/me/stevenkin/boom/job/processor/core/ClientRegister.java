package me.stevenkin.boom.job.processor.core;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.exception.ZKConnectException;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ClientRegister implements Lifecycle {
    private static final String ZKPREFIX = "/boom/app";

    private final Lock RESTART_LOCK = new ReentrantLock();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private CuratorFramework framework;

    private String zkHosts;

    private String namespace;

    private String appName;

    private String clientId;

    public ClientRegister(String zkHosts, String namespace, String clientId) {
        this.zkHosts = zkHosts;
        this.namespace = namespace;
        this.clientId = clientId;
    }

    private boolean started = false;

    @Override
    public void start() {
        if (started)
            return;
        connectZk();
        scheduler.scheduleAtFixedRate(() -> {
            String appPath = PathKit.format(ZKPREFIX, appName, clientId);
            try {
                if (framework.checkExists().forPath(appPath) == null){
                    framework.create().withMode(CreateMode.EPHEMERAL).forPath(appPath, null);
                }
            } catch (Exception e) {
                log.error("client heartbeat happen error {}", e);
                reconnectZk();
            }
        }, 1, 10, TimeUnit.SECONDS);
        started = true;
    }

    @Override
    public void shutdown() {
        if (!started)
            return;
        scheduler.shutdown();
        if (framework != null){
            framework.close();
        }
        started = false;
    }

    private void connectZk(){
        framework = CuratorFrameworkFactory.builder()
                .connectString(zkHosts)
                .namespace(namespace)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        framework.start();

        try {
            framework.blockUntilConnected(30, TimeUnit.SECONDS);
            log.info("client {} connect zk successfully", clientId);
        } catch (InterruptedException e) {
            throw new ZKConnectException(e);
        }
    }

    private void reconnectZk(){
        try {

            boolean locked = RESTART_LOCK.tryLock(30, TimeUnit.SECONDS);
            if (!locked){
                log.warn("timeout to get the restart lock, maybe it's locked by another.");
                return;
            }

            if (framework.getZookeeperClient().isConnected()){
                return;
            }

            if (framework != null){
                // close old connection
                framework.close();
            }

            connectZk();

        } catch (InterruptedException e) {
            log.error("failed to get the restart lock, cause: {}", Throwables.getStackTraceAsString(e));
        } finally {
            RESTART_LOCK.unlock();
        }
    }
}
