package me.stevenkin.boom.job.common.zk;

import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.kit.PathKit;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Slf4j
public class SimpleZkQueue implements ZkQueue{

    private ZkClient zkClient;

    private String path;

    private PathChildrenCache cache;

    private CountDownLatch latch;

    private List<ZkElement> queue;

    private Lock lock;

    private Condition condition;

    public SimpleZkQueue(ZkClient zkClient, String path) throws Exception{
        this.zkClient = zkClient;
        this.path = path;
        this.latch = new CountDownLatch(1);
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        cache = zkClient.addNodeAddListener(path, (p, data) -> {
            try {
                latch.await();
                put(new ZkElement(PathKit.lastNode(p), data));
            } catch (InterruptedException e) {
                log.error("some error happen", e);
            }

        });
        cache.start();
        queue = new LinkedList<>();
        List<String> nodes = zkClient.gets(path);
        if (!Iterables.isEmpty(nodes)) {
            nodes.forEach(n -> {
                String nodePath = PathKit.format(path, n);
                queue.add(new ZkElement(n, zkClient.get(nodePath)));
            });
        }
        latch.countDown();
    }

    @Override
    public void put(ZkElement element) {
        try {
            lock.lock();
            if (queue.contains(element))
                return;
            queue.add(element);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public ZkElement take() {
        try {
            lock.lock();
            while (queue.isEmpty()) {
                condition.await();
            }
            return queue.remove(0);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return queue.size();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        cache.close();
    }
}
