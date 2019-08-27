package me.stevenkin.boom.job.common.zk;

public interface ZkQueue {

    void put(ZkElement element);

    ZkElement take();

    int size();

    void close() throws Exception;
}
