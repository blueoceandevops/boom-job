package me.stevenkin.boom.job.cluster;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

public class JobSchedulerCluster implements Cluster {
    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new JobSchedulerClusterInvoker<>(directory);
    }
}
