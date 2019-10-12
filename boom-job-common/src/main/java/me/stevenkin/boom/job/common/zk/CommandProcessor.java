package me.stevenkin.boom.job.common.zk;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.data.Stat;

import java.util.List;

@Slf4j
public class CommandProcessor extends Lifecycle {
    @Setter
    private Lifecycle component;
    @Setter
    private ZkClient zkClient;
    @Setter
    private String commandPath;
    @Setter
    private String id;

    private NodeCache nodeCache;

    public CommandProcessor() {
    }

    private String nodeCacheListenPath(String commandPath, String id) {
        return commandPath + "/" + id;
    }

    @Override
    public void doStart() throws Exception {
        nodeCache = zkClient.registerNodeCacheListener(nodeCacheListenPath(commandPath, id), new NodeListener() {
            @Override
            public void onChange(String path, Stat stat, byte[] data) {
                String cmd = new String(data);
                processCommand(cmd, path);
            }

            @Override
            public void onDelete() {

            }
        });
        nodeCache.start();
        initProcess();
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

    private void initProcess() {
        List<String> cmds = zkClient.gets(commandPath);
        String cmd = null;
        String path = null;
        if (cmds != null && !cmds.isEmpty()) {
            for (String c : cmds) {
                if (PathKit.lastNode(c).equals(id)){
                    cmd = new String(zkClient.get(c));
                    path = c;
                    break;
                }
            }
            if (!StringUtils.isEmpty(cmd) && !StringUtils.isEmpty(path)) {
                processCommand(cmd, path);
            }
        }
    }

    private synchronized void processCommand(String cmd, String path) {
        if (StringUtils.isEmpty(cmd) || StringUtils.isEmpty(path))
            return;
        if (!zkClient.checkExists(path))
            return;
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
}
