package me.stevenkin.boom.job.processor.core;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.AppInfo;
import me.stevenkin.boom.job.common.dto.RegisterResponse;
import me.stevenkin.boom.job.common.exception.ZkException;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.kit.ZkKit;
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
    private static final String ZKPREFIX = "client";

    private final Lock RESTART_LOCK = new ReentrantLock();

    private BoomJobClient jobClient;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private CuratorFramework framework;

    private String zkHosts;

    private String namespace;

    private String author;

    private String appName;

    private String clientId;

    private RegisterService registerService;

    public ClientRegister(BoomJobClient jobClient) {
        this.jobClient = jobClient;
        this.zkHosts = jobClient.zkHosts();
        this.namespace = jobClient.namespace();
        this.author = jobClient.author();
        this.appName = jobClient.appName();
        this.clientId = jobClient.clientId();
        this.registerService = jobClient.registerService();
    }

    private boolean started = false;

    @Override
    public void start() {
        if (started)
            return;
        connectZk();
        String appId = NameKit.getAppId(appName, author);
        ZkKit.mkdir(PathKit.format(jobClient.namespace(), ZKPREFIX, appId), framework);
        scheduler.scheduleAtFixedRate(() -> {
            String appPath = PathKit.format(ZKPREFIX, appId, clientId);
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
            throw new ZkException(e);
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
