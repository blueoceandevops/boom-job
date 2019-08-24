package me.stevenkin.boom.job.common.zk;

@FunctionalInterface
public interface NodeAddListener {

    void onAdd(String path, byte[] data);

}
