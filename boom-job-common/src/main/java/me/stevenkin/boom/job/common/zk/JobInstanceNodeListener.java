package me.stevenkin.boom.job.common.zk;

import com.alibaba.fastjson.JSON;
import org.apache.zookeeper.data.Stat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JobInstanceNodeListener implements NodeListener {
    private Map<Predicate<JobInstanceNode>, Consumer<JobInstanceNode>> map = new HashMap<>();
    @Override
    public void onChange(String path, Stat stat, byte[] data) {
        JobInstanceNode node = JSON.parseObject(new String(data), JobInstanceNode.class);
        map.entrySet().stream().filter(e -> e.getKey().test(node)).map(Map.Entry::getValue).forEach(c -> c.accept(node));
    }

    @Override
    public void onDelete() {

    }

    @Override
    public NodeListener add(Predicate<?> predicate, Consumer<?> action) {
        map.put((Predicate<JobInstanceNode>) predicate, (Consumer<JobInstanceNode>) action);
        return this;
    }
}
