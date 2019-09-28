package me.stevenkin.boom.job.common.zk;

import org.apache.zookeeper.data.Stat;

public interface NodeListener {
    void onChange(String path, Stat stat, byte[] data);

    void onDelete();
}
