package me.stevenkin.boom.job.test.common;

import me.stevenkin.boom.job.common.zk.ZkClient;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.concurrent.CountDownLatch;

public class NodeCacheTest {
    private static final String PATH = "/example/node";

    public static void main(String[] args) throws Exception {
        ZkClient client = new ZkClient("127.0.0.1:2181", "test");
        client.start();
        client.deleteIfExists(PATH);
        System.out.println("create node");
        client.create("/example/node", "hello".getBytes());
        Thread.sleep(1000);
        client.registerNodeCacheListener(PATH, (p, s, d) -> {
            System.out.println("Path: " + p);
            System.out.println("Stat:" + s);
            System.out.println("Data: "+ new String(d));
        }).start(true);
        Thread.sleep(1000);
        System.out.println("delete node");
        client.delete("/example/node");
        System.in.read();
    }

}
