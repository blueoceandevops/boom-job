package me.stevenkin.boom.job.test.scheduler;

import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.zk.ZkClient;

public class NoExistNodeTest {
    private static final String PATH = "/example/node";

    public static void main(String[] args) throws Exception {
        ZkClient client = new ZkClient("47.99.214.70:2181", "test");
        client.start();
        client.deleteIfExists(PATH);
        System.out.println("create node");
        client.create("/example/node", "hello".getBytes());
        Thread.sleep(1000);
        System.out.println("delete node");
        client.delete("/example/node");
        String data = new String(client.get(PATH));
        System.out.println("data: " + data);
        System.in.read();
    }
}
