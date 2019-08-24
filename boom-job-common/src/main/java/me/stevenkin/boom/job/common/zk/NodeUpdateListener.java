package me.stevenkin.boom.job.common.zk;

@FunctionalInterface
public interface NodeUpdateListener {

    void onUpdate(String path, byte[] newData);

}
