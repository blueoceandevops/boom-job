package me.stevenkin.boom.job.scheduler.cluster;

import me.stevenkin.boom.job.common.dubbo.Node;

import java.util.List;

public interface ClientCluster extends Cluster {
    /**
     * get all node list of a app
     * @param app
     * @return node list
     */
    List<Node> getNodesByApp(String app);
}
