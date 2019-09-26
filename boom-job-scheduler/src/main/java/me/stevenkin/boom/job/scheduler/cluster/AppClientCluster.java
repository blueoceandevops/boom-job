package me.stevenkin.boom.job.scheduler.cluster;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class AppClientCluster extends Lifecycle {
    private static final String CLIENT = "client";
    private static final String CLIENT_FAILOVER = "failover/client";

    private ZkClient zkClient;

    private String app;

    private PathChildrenCache addCache;

    private PathChildrenCache delCache;

    private Set<String> alives = new HashSet<>();

    public AppClientCluster(ZkClient zkClient, String app) {
        this.zkClient = zkClient;
        this.app = app;
    }

    @Override
    public void doStart() throws Exception {
        String path = PathKit.format(CLIENT, app);
        addCache = zkClient.addNodeAddListener(path, (p, data) ->
                addClient(PathKit.lastNode(p))
        );
        delCache = zkClient.addNodeDeleteListener(path, p -> {
            String node = PathKit.lastNode(p);
            deleteClient(node);
            zkClient.create(PathKit.format(CLIENT_FAILOVER, node));
        });
        try {
            addCache.start();
            delCache.start();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        //listen first, then traversing
        List<String> clientIds = zkClient.gets(path);
        if (clientIds != null && !clientIds.isEmpty()) {
            clientIds.forEach(this::addClient);
        }
    }

    @Override
    public void doPause() throws Exception {
        addCache.close();
        delCache.close();
        alives.clear();
    }

    @Override
    public void doResume() throws Exception {
        doStart();
    }

    @Override
    public void doShutdown() throws Exception {

    }

    public Set<String> getAlives() {
        return Sets.newHashSet(alives);
    }

    private synchronized void addClient(String clientId) {
        if (!alives.contains(clientId)) {
            alives.add(clientId);
        }
    }

    private synchronized void deleteClient(String clientId) {
        alives.remove(clientId);
    }
}
