package me.stevenkin.boom.job.scheduler.cluster;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class SchedulerCluster extends Lifecycle {

    private static final String SCHEDULER = "scheduler";
    private static final String SCHEDULER_FAILOVER = "failover/scheduler";

    @Autowired
    private ZkClient zkClient;

    private PathChildrenCache addCache;

    private PathChildrenCache delCache;

    private Set<String> alives = new HashSet<>();

    public Set<String> getAlives() {
        return Sets.newHashSet(alives);
    }

    private synchronized void addScheduler(String schedulerId) {
        if (!alives.contains(schedulerId)) {
            alives.add(schedulerId);
        }
    }

    private synchronized void deleteScheduler(String scheduler) {
        alives.remove(scheduler);
    }

    @Override
    public void doStart() throws Exception {
        String path = PathKit.format(SCHEDULER);
        addCache = zkClient.addNodeAddListener(path, (p, data) ->
                addScheduler(PathKit.lastNode(p))
        );
        delCache = zkClient.addNodeDeleteListener(path, p -> {
            deleteScheduler(PathKit.lastNode(p));
            zkClient.create(PathKit.format(SCHEDULER_FAILOVER, PathKit.lastNode(p)));
        });
        addCache.start();
        delCache.start();
        //listen first, then traversing
        List<String> schedulers = zkClient.gets(path);
        if (schedulers != null && !schedulers.isEmpty()) {
            schedulers.forEach(this::addScheduler);
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
}
