package me.stevenkin.boom.job.common.zk;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.exception.ZkException;
import me.stevenkin.boom.job.common.support.Lifecycle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ZkClient {
    private static final ExponentialBackoffRetry DEFAULT_RETRY_STRATEGY = new ExponentialBackoffRetry(1000, 3);

    private CuratorFramework client;

    private String zkHosts;

    private String namespace;

    private volatile boolean started = false;

    private final Lock RESTART_LOCK = new ReentrantLock();

    public ZkClient(String zkHosts, String namespace) {
        this.zkHosts = zkHosts;
        this.namespace = namespace;
    }

    public synchronized void start() throws Exception{
        if (started){
            return;
        }
        doStart();
    }

    private void doStart(){

        client = CuratorFrameworkFactory.builder()
                .connectString(zkHosts)
                .namespace(namespace)
                .retryPolicy(DEFAULT_RETRY_STRATEGY)
                .build();

        client.start();

        try {
            // connected until
            client.blockUntilConnected(30, TimeUnit.SECONDS);
            started = true;
        } catch (InterruptedException e) {
            throw new ZkException(e);
        }
    }

    public void restart(){

        try {

            boolean locked = RESTART_LOCK.tryLock(30, TimeUnit.SECONDS);
            if (!locked){
                log.warn("timeout to get the restart lock, maybe it's locked by another.");
                return;
            }

            if (client.getZookeeperClient().isConnected()){
                return;
            }

            if (client != null){
                // close old connection
                client.close();
            }

            doStart();

        } catch (InterruptedException e) {
            log.error("failed to get the restart lock, cause: {}", Throwables.getStackTraceAsString(e));
        } finally {
            RESTART_LOCK.unlock();
        }
    }

    public synchronized void shutdown() throws Exception {
        if (started && client != null){
            client.close();
            started = false;
        }
    }

    /**
     * Create an persistent path
     * @param path path
     * @return the path created
     */
    public String create(String path) {
        return create(path, (byte[])null);
    }

    /**
     * Create an persistent path
     * @param path path
     * @param data byte data
     * @return the path created
     */
    public String create(String path, byte[] data) {
        try {
            return client.create().withMode(CreateMode.PERSISTENT).forPath(path, data);
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an persistent path
     * @param path path
     * @param data string data
     * @return the path created
     */
    public String create(String path, String data){
        try {
            return create(path, data.getBytes("UTF-8"));
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an persistent path, save the object to json
     * @param path path
     * @param obj object
     * @return the path created
     */
    public String create(String path, Object obj){
        return create(path, JSON.toJSONString(obj));
    }

    /**
     * Create an persistent path
     * @param path path
     * @param data byte data
     * @return the path created
     */
    public String createSequential(String path, byte[] data) {
        try {
            return client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, data);
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an persistent path
     * @param path path
     * @param data byte data
     * @return the path created
     */
    public String createSequential(String path, String data) {
        try {
            return createSequential(path, data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an persistent path
     * @param path path
     * @param obj a object
     * @return the path created
     */
    public String createSequentialJson(String path, Object obj) {
        try {
            return createSequential(path, JSON.toJSONString(obj).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }


    /**
     * Create an ephemeral path
     * @param path path
     * @return the path created
     */
    public String createEphemeral(String path) {
        return createEphemeral(path, (byte[]) null);
    }

    /**
     * Create an ephemeral path
     * @param path path
     * @param data byte data
     * @return the path created
     */
    public String createEphemeral(String path, byte[] data) {
        try {
            return client.create().withMode(CreateMode.EPHEMERAL).forPath(path, data);
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an ephemeral path
     * @param path path
     * @param data string data
     * @return the path created
     */
    public String createEphemeral(String path, String data){
        try {
            return client.create().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes("UTF-8"));
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an ephemeral path
     * @param path path
     * @param data data
     * @return the path created
     */
    public String createEphemeral(String path, Integer data) {
        return createEphemeral(path, data.toString());
    }

    /**
     * Create an ephemeral path
     * @param path path
     * @param obj object data
     * @return the path created
     */
    public String createEphemeral(String path, Object obj) {
        return createEphemeral(path, JSON.toJSONString(obj));
    }

    /**
     * Create an ephemeral path
     * @param path path
     * @param data byte data
     * @return the path created
     * @throws Exception
     */
    public String createEphemeralSequential(String path, byte[] data) {
        try {
            return client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, data);
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an ephemeral path
     * @param path path
     * @param data string data
     * @return the path created
     * @throws Exception
     */
    public String createEphemeralSequential(String path, String data) {
        try {
            return createEphemeralSequential(path, data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create an ephemeral and sequential path
     * @param path path
     * @param obj object
     * @return the path created
     * @throws Exception
     */
    public String createEphemeralSequential(String path, Object obj) {
        return createEphemeralSequential(path, JSON.toJSONString(obj));
    }

    /**
     * Create a node if not exists
     * @param path path
     * @param data path data
     * @return return true if create
     * @throws Exception
     */
    public Boolean createIfNotExists(String path, String data) {
        try {
            return createIfNotExists(path, data.getBytes("UTF-8"));
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Create a node if not exists
     * @param path path
     * @return return true if create
     */
    public Boolean createIfNotExists(String path) {
        return createIfNotExists(path, (byte[])null);
    }

    /**
     * Create a node if not exists
     * @param path path
     * @param data path data
     * @return return true if create
     */
    public Boolean createIfNotExists(String path, byte[] data) {
        try {
            Stat pathStat = client.checkExists().forPath(path);
            if (pathStat == null){
                String nodePath = client.create().forPath(path, data);
                return Strings.isNullOrEmpty(nodePath) ? Boolean.FALSE : Boolean.TRUE;
            }
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }

        return Boolean.FALSE;
    }

    /**
     * Check the path exists or not
     * @param path the path
     * @return return true if the path exists, or false
     */
    public Boolean checkExists(String path){
        try {
            Stat pathStat = client.checkExists().forPath(path);
            return pathStat != null;
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Make directories if necessary
     * @param dir the dir
     * @return return true if mkdirs successfully, or throw ZkException
     */
    public Boolean mkdirs(String dir){
        try {
            EnsurePath clientAppPathExist =
                    new EnsurePath("/" + client.getNamespace() + slash(dir));
            clientAppPathExist.ensure(client.getZookeeperClient());
            return Boolean.TRUE;
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    public Boolean update(String path, Integer data){
        return update(path, data.toString());
    }

    public Boolean update(String path, Object data){
        return update(path, JSON.toJSONString(data));
    }

    public Boolean update(String path, String data){
        try {
            return update(path, data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ZkException(e);
        }
    }

    public Boolean update(String path){
        return update(path, (byte[])null);
    }

    public Boolean update(String path, byte[] data){
        try {
            client.setData().forPath(path, data);
            return Boolean.TRUE;
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Delete the node
     * @param path node path
     */
    public void delete(String path) {
        try {
            client.delete().forPath(path);
        } catch (Exception e){
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Delete the node if the node exists
     * @param path node path
     */
    public void deleteIfExists(String path) {
        try {
            if(checkExists(path)){
                delete(path);
            }
        } catch (Exception e){
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Delete the node recursively
     * @param path the node path
     */
    public void deleteRecursively(String path){
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e){
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * Delete the node recursively if the path exists
     * @param path the node path
     */
    public void deleteRecursivelyIfExists(String path){
        try {
            if(checkExists(path)){
                deleteRecursively(path);
            }
        } catch (Exception e){
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * get the data of path
     * @param path the node path
     * @return the byte data of the path
     */
    public byte[] get(String path){
        try {
            return client.getData().forPath(path);
        } catch (Exception e){
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    /**
     * get the node data as string
     * @param path path data
     * @return return the data string or null
     */
    public String getString(String path){
        byte[] data = get(path);
        if (data != null){
            try {
                return new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public Integer getInteger(String nodePath) {
        String nodeValue = getString(nodePath);
        return Strings.isNullOrEmpty(nodeValue) ? null : Integer.parseInt(nodeValue);
    }

    /**
     * get the node data as an object
     * @param path node path
     * @param clazz class
     * @return json object or null
     */
    public <T> T getJson(String path, Class<T> clazz){
        byte[] data = get(path);
        if (data != null){
            try {
                String json = new String(data, "UTF-8");
                return JSON.parseObject(json, clazz);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Get the children of the path
     * @param path the path
     * @return the children of the path
     */
    public List<String> gets(String path){
        try {

            if (!checkExists(path)){
                return Collections.emptyList();
            }

            return client.getChildren().forPath(slash(path));
        } catch (Exception e) {
            handleConnectionLoss(e);
            throw new ZkException(e);
        }
    }

    public PathChildrenCache addNodeAddListener(String path, final NodeAddListener listener) {
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.getListenable().addListener((client, event) -> {
                PathChildrenCacheEvent.Type eventType = event.getType();
                ChildData childData = event.getData();
                if (childData == null){
                    return;
                }
                String path1 = childData.getPath();

                switch (eventType) {
                    case CHILD_ADDED:
                        listener.onAdd(path1, childData.getData());
                        break;
                    case CHILD_REMOVED:
                        break;
                    case CHILD_UPDATED:
                        break;
                    case CONNECTION_RECONNECTED:
                        cache.rebuild();
                    default:
                        break;
                }
        });
        return cache;
    }

    public PathChildrenCache addNodeDeleteListener(String path, final NodeDeleteListener listener) {
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.getListenable().addListener((client, event) -> {
            PathChildrenCacheEvent.Type eventType = event.getType();
            ChildData childData = event.getData();
            if (childData == null){
                return;
            }
            String path1 = childData.getPath();

            switch (eventType) {
                case CHILD_ADDED:
                    break;
                case CHILD_REMOVED:
                    listener.onDelete(path1);
                    break;
                case CHILD_UPDATED:
                    break;
                case CONNECTION_RECONNECTED:
                    cache.rebuild();
                default:
                    break;
            }
        });
        return cache;
    }

    public PathChildrenCache addNodeUpdateListener(String path, final NodeUpdateListener listener) {
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.getListenable().addListener((client, event) -> {
            PathChildrenCacheEvent.Type eventType = event.getType();
            ChildData childData = event.getData();
            if (childData == null){
                return;
            }
            String path1 = childData.getPath();

            switch (eventType) {
                case CHILD_ADDED:
                    break;
                case CHILD_REMOVED:
                    break;
                case CHILD_UPDATED:
                    listener.onUpdate(path1, childData.getData());
                    break;
                case CONNECTION_RECONNECTED:
                    cache.rebuild();
                default:
                    break;
            }
        });
        return cache;
    }

    private String slash(String path){
        return path.startsWith("/") ? path : "/" + path;
    }

    private void handleConnectionLoss(Exception e){
        if (e instanceof KeeperException.ConnectionLossException){

            log.warn("zk client will restart...");

            // try to restart the zk connection
            restart();

            log.warn("zk client do restart finished.");
        }
    }


}
