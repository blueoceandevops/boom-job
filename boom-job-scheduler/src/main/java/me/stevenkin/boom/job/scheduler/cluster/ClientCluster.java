package me.stevenkin.boom.job.scheduler.cluster;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
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
public class ClientCluster implements InitializingBean, DisposableBean{
    private static final String CLIENT = "cluster/client";

    @Autowired
    private ZkClient zkClient;

    private PathChildrenCache cache;

    private Map<String, AppClientCluster> appClientClusterMap = new HashMap<>();

    @Override
    public void destroy() throws Exception {
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException e) {
            }
        }
        appClientClusterMap.values().forEach(AppClientCluster::shutdown);
        if (zkClient != null) {
            zkClient.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String path = PathKit.format(CLIENT);
        cache = zkClient.addNodeAddListener(path, (p, data) -> {
            String app = PathKit.lastNode(p);
            addAppClientCluster(app);
        });
        cache.start();
        //注意顺序，先监听，后遍历
        List<String> apps = zkClient.gets(path);
        if (apps != null && !apps.isEmpty()) {
            apps.forEach(this::addAppClientCluster);
        }
    }

    private synchronized void addAppClientCluster(String app) {
        if (!appClientClusterMap.containsKey(app)) {
            AppClientCluster appClientCluster = new AppClientCluster(zkClient, app);
            appClientCluster.start();
            appClientClusterMap.put(app, appClientCluster);
        }
    }
}
