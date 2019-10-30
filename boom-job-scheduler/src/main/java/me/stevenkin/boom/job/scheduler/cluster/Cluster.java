package me.stevenkin.boom.job.scheduler.cluster;

import com.alibaba.dubbo.common.URL;
import me.stevenkin.boom.job.common.dubbo.Node;

import java.util.List;

public interface Cluster {
    /**
     * get all nodes
     * @return all nodes
     */
    List<Node> getAllNodes();

    /**
     * transform dubbo url to cluster node
     * @param urls
     */
    void updateNodes(List<URL> urls);


}
