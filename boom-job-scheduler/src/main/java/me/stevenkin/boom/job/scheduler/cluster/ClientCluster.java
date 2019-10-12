package me.stevenkin.boom.job.scheduler.cluster;

import com.alibaba.dubbo.common.URL;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ClientCluster extends Lifecycle {
    private static final String INTERFACE = "me.stevenkin.boom.job.common.service.ClientProcessor";

    private static final String PROVIDERS = "providers";

    @Autowired
    private ZkClient zkClient;

    private PathChildrenCache addCache;

    private PathChildrenCache removeCache;

    private Map<String, List<String>> appClientMap = new HashMap<>();

    @Override
    public void doStart() throws Exception {
        String path = PathKit.format(INTERFACE, PROVIDERS);
        zkClient.mkdirs(path);
        addCache = zkClient.addNodeAddListener(path, (p, data) -> {
            String url = PathKit.lastNode(p);
            addAppClient(url);
        });
        removeCache = zkClient.addNodeAddListener(path, (p, data) -> {
            String url = PathKit.lastNode(p);
            removeAppClient(url);
        });
        //listen first, then traversing
        addCache.start();
        removeCache.start();
        List<String> paths = zkClient.gets(path);
        if (paths != null && !paths.isEmpty()) {
            paths.stream().map(PathKit::lastNode).forEach(this::addAppClient);
        }
    }

    @Override
    public void doPause() throws Exception {
        if (addCache != null) {
            try {
                addCache.close();
            } catch (IOException e) {
            }
        }
        if (removeCache != null) {
            try {
                removeCache.close();
            } catch (IOException e) {
            }
        }
        appClientMap.clear();
    }

    @Override
    public void doResume() throws Exception {
        doStart();
    }

    @Override
    public void doShutdown() throws Exception {

    }

    private synchronized void addAppClient(String url) {
        String app = getGroup(url);
        String clientId = getClientId(url);
        List<String> clientIds = appClientMap.get(app);
        if (clientIds == null) {
            clientIds = new ArrayList<>();
        }
        if (!clientIds.contains(clientId)) {
            clientIds.add(clientId);
        }
        appClientMap.put(app, clientIds);
    }

    private synchronized void removeAppClient(String url) {
        String app = getGroup(url);
        String clientId = getClientId(url);
        List<String> clientIds = appClientMap.get(app);
        if (clientIds != null) {
            clientIds.remove(clientId);
        }
    }

    private String getGroup(String url) {
        URL url1 = URL.valueOf(url);
        return url1.getParameterAndDecoded("group");

    }

    private String getClientId(String url) {
        URL url1 = URL.valueOf(url);
        String timestamp = url1.getParameter("timestamp");
        if (StringUtils.isEmpty(timestamp)) {
            return null;
        }
        Integer.parseInt(timestamp);
        return url1.getHost() + "_" + url1.getPort() + "_" + timestamp;
    }
}
