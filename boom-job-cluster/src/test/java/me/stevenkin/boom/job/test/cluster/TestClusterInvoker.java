package me.stevenkin.boom.job.test.cluster;

import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.FailfastClusterInvoker;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestClusterInvoker<T> extends FailfastClusterInvoker<T> {
    public TestClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    public Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        checkInvokers(invokers, invocation);
        Class<?> interf = invokers.get(0).getInterface();
        String method = invocation.getMethodName();
        Class<?>[] types = invocation.getParameterTypes();
        if (interf.equals(TestService.class) && method.equals("hello") && types != null && types.length == 1 && types[0].equals(String.class)) {
            List<Result> results = new ArrayList<>();
            List<Throwable> throwables = new ArrayList<>();
            for (Invoker<T> invoker : invokers) {
                try {
                    log.info("invoker >>>" + invoker.getUrl().toFullString());
                    results.add(invoker.invoke(invocation));
                }catch (Throwable e) {
                    throwables.add(e);
                }
            }
            throwables.forEach(e -> log.error(e.getMessage()));
            StringBuilder stringBuilder = new StringBuilder();
            results.forEach(r -> stringBuilder.append(r.getValue()));
            Result result = new RpcResult(stringBuilder.toString());
            return result;
        }
        return super.doInvoke(invocation, invokers, loadbalance);
    }
}
