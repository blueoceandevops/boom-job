package me.stevenkin.boom.job.common.zk;

import com.alibaba.fastjson.JSON;
import me.stevenkin.boom.job.common.support.ActionOnCondition;
import me.stevenkin.boom.job.common.zk.model.JobInstanceNode;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

public class JobInstanceNodeListener implements NodeListener<JobInstanceNode, JobInstanceNode> {
    private List<ActionOnCondition<JobInstanceNode, JobInstanceNode>> actionOnConditions = new ArrayList<>();
    @Override
    public void onChange(String path, Stat stat, byte[] data) {
        JobInstanceNode node = JSON.parseObject(new String(data), JobInstanceNode.class);
        actionOnConditions.stream().filter(ac -> ac.test(node)).forEach(ac -> ac.action(node));
    }

    @Override
    public void onDelete() {

    }

    @Override
    public JobInstanceNodeListener add(ActionOnCondition<JobInstanceNode, JobInstanceNode> actionOnCondition) {
        actionOnConditions.add(actionOnCondition);
        return this;
    }
}
