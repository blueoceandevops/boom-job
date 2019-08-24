package me.stevenkin.boom.job.scheduler.cluster;

import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ClientFailover implements InitializingBean, DisposableBean{
    private static final String CLIENT = "client";

    private static final String CLIENT_FAILOVER = "failover/client";

    @Autowired
    private ZkClient zkClient;

    private PathChildrenCache cache;


    @Override
    public void destroy() throws Exception {
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException e) {
            }
        }
        if (zkClient != null) {
            zkClient.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        zkClient.mkdirs(PathKit.format(zkClient.getNamespace(), CLIENT));
        zkClient.mkdirs(PathKit.format(zkClient.getNamespace(), CLIENT_FAILOVER));
        cache = zkClient.addNodeDeleteListener(PathKit.format(CLIENT), path -> {
            String node = PathKit.lastNode(path);
            zkClient.create(PathKit.format(CLIENT_FAILOVER, node));
        });
        cache.start();
    }
}
