package me.stevenkin.boom.job.common.zk;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.support.Lifecycle;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.data.Stat;

@Slf4j
public class CommandProcessor extends Lifecycle {
    private Lifecycle component;

    private ZkClient zkClient;

    private String commandPath;

    private String id;

    private NodeCache nodeCache;

    public CommandProcessor(Lifecycle component, ZkClient zkClient, String commandPath, String id) {
        this.component = component;
        this.zkClient = zkClient;
        this.commandPath = commandPath;
        this.id = id;
        this.nodeCache = zkClient.registerNodeCacheListener(nodeCacheListenPath(commandPath, id), new NodeListener() {
            @Override
            public void onChange(String path, Stat stat, byte[] data) {
                String cmd = new String(data);
                try {
                    switch (cmd) {
                        case "pause":
                            component.pause();
                            break;
                        case "resume":
                            component.resume();
                            break;
                        default:
                            log.warn("command {} is not supported", cmd);
                    }
                } catch (Exception e) {
                    log.error("component status change happen error", e);
                }
                zkClient.delete(path);
            }

            @Override
            public void onDelete() {

            }
        });
    }

    private String nodeCacheListenPath(String commandPath, String id) {
        return commandPath + "/" + id;
    }

    @Override
    public void doStart() throws Exception {
        nodeCache.start();
    }

    @Override
    public void doPause() throws Exception {

    }

    @Override
    public void doResume() throws Exception {

    }

    @Override
    public void doShutdown() throws Exception {
        nodeCache.close();
    }
}
