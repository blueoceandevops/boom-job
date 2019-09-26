package me.stevenkin.boom.job.scheduler.cluster;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ClientCluster extends Lifecycle {
    private static final String CLIENT = "client";

    @Autowired
    private ZkClient zkClient;

    private PathChildrenCache cache;

    private Map<String, AppClientCluster> appClientClusterMap = new HashMap<>();

    private synchronized void addAppClientCluster(String app) {
        if (!appClientClusterMap.containsKey(app)) {
            AppClientCluster appClientCluster = new AppClientCluster(zkClient, app);
            try {
                appClientCluster.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            appClientClusterMap.put(app, appClientCluster);
        }
    }

    @Override
    public void doStart() throws Exception {
        String path = PathKit.format(CLIENT);
        cache = zkClient.addNodeAddListener(path, (p, data) -> {
            String app = PathKit.lastNode(p);
            addAppClientCluster(app);
        });
        cache.start();
        //listen first, then traversing
        List<String> apps = zkClient.gets(path);
        if (apps != null && !apps.isEmpty()) {
            apps.forEach(this::addAppClientCluster);
        }
    }

    @Override
    public void doPause() throws Exception {
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException e) {
            }
        }
        appClientClusterMap.values().forEach(app -> {
            try {
                app.pause();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        appClientClusterMap.clear();
    }

    @Override
    public void doResume() throws Exception {
        doStart();
    }

    @Override
    public void doShutdown() throws Exception {

    }
}
