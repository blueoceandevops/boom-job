package me.stevenkin.boom.job.scheduler.cluster;

import com.alibaba.dubbo.common.URL;
import me.stevenkin.boom.job.common.dubbo.Node;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.kit.URLKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.scheduler.dubbo.DubboRegistryCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SimpleSchedulerCluster extends Lifecycle implements Cluster {
    private static final String FAILOVER_PATH = "/failover/scheduler";

    private static final String SERVICE = "me.stevenkin.boom.job.common.service.JobSchedulerService";

    @Autowired
    private DubboRegistryCache registryCache;

    @Autowired
    private ZkClient zkClient;

    private List<Node> nodes = new ArrayList<>();

    private RegistryListener listener = this::updateNodes;

    @Override
    public void doStart() throws Exception {
        registryCache.registerListener(SERVICE, listener);
    }

    @Override
    public void doPause() throws Exception {
        registryCache.unregisterListener(SERVICE, listener);
        nodes.clear();
    }

    @Override
    public void doResume() throws Exception {
        doStart();
    }

    @Override
    public void doShutdown() throws Exception {

    }

    @Override
    public synchronized List<Node> getAllNodes() {
        return new ArrayList<>(nodes);
    }

    @Override
    public synchronized void updateNodes(List<URL> urls) {
        if (urls == null)
            urls = new ArrayList<>();
        List<Node> nodeList = urls.stream()
                .map(URLKit::urlToNode)
                .collect(Collectors.toList());
        nodes.stream().filter(node -> !nodeList.contains(node)).forEach(node ->
                zkClient.create(PathKit.format(FAILOVER_PATH, node.toString()))
        );
        nodes = nodeList;
    }
}
