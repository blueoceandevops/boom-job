package me.stevenkin.boom.job.common.zk;

import me.stevenkin.boom.job.common.support.ActionOnCondition;
import org.apache.zookeeper.data.Stat;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface NodeListener<C, P> {
    void onChange(String path, Stat stat, byte[] data);

    void onDelete();

    NodeListener<C, P> add(ActionOnCondition<C, P> actionOnCondition);
}
