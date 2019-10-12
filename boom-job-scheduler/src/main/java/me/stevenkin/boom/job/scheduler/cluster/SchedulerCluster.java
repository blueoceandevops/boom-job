package me.stevenkin.boom.job.scheduler.cluster;

import com.alibaba.dubbo.common.URL;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.NameKit;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class SchedulerCluster extends Lifecycle {
    private static final String FAILOVER_PATH = "/failover/scheduler";

    private static final String INTERFACE = "me.stevenkin.boom.job.common.service.JobSchedulerService";

    private static final String PROVIDERS = "providers";

    @Autowired
    private ZkClient zkClient;

    private PathChildrenCache addCache;

    private PathChildrenCache removeCache;

    private Set<String> alives = new HashSet<>();

    public Set<String> getAlives() {
        return Collections.unmodifiableSet(alives);
    }

    @Override
    public void doStart() throws Exception {
        String path = PathKit.format(INTERFACE, PROVIDERS);
        addCache = zkClient.addNodeAddListener(path, (p, data) ->
                addScheduler(PathKit.lastNode(p))
        );
        removeCache = zkClient.addNodeDeleteListener(path, p -> {
            deleteScheduler(PathKit.lastNode(p));
        });
        //listen first, then traversing
        addCache.start();
        removeCache.start();

        List<String> schedulers = zkClient.gets(path);
        if (schedulers != null && !schedulers.isEmpty()) {
            schedulers.forEach(this::addScheduler);
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
        alives.clear();
    }

    @Override
    public void doResume() throws Exception {
        doStart();
    }

    @Override
    public void doShutdown() throws Exception {

    }

    private synchronized void addScheduler(String url) {
        String schedulerId = NameKit.getNodeId(url);
        if (!alives.contains(schedulerId)) {
            alives.add(schedulerId);
        }
    }

    private synchronized void deleteScheduler(String url) {
        String schedulerId = NameKit.getNodeId(url);
        alives.remove(schedulerId);
        zkClient.create(PathKit.format(FAILOVER_PATH, schedulerId));
    }
}
