package me.stevenkin.boom.job.scheduler.cluster;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
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
public class SchedulerCluster implements InitializingBean, DisposableBean {

    private static final String SCHEDULER = "cluster/scheduler";
    private static final String SCHEDULER_FAILOVER = "cluster/failover/scheduler";

    @Autowired
    private ZkClient zkClient;

    private PathChildrenCache addCache;

    private PathChildrenCache delCache;

    private Set<String> alives = new HashSet<>();

    @Override
    public void destroy() throws Exception {
        try {
            addCache.close();
            delCache.close();
        } catch (IOException e) {
        }
        if (zkClient != null) {
            zkClient.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
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
        //注意顺序，先监听，后遍历
        List<String> schedulers = zkClient.gets(path);
        if (schedulers != null && !schedulers.isEmpty()) {
            schedulers.forEach(this::addScheduler);
        }
    }

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
}
