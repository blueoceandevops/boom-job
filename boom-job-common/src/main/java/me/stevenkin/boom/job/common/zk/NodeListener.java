package me.stevenkin.boom.job.common.zk;

import org.apache.zookeeper.data.Stat;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface NodeListener {
    void onChange(String path, Stat stat, byte[] data);

    void onDelete();

    NodeListener add(Predicate<?> predicate, Consumer<?> action);
}
