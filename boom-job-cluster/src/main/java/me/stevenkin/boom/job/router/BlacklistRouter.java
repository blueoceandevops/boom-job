package me.stevenkin.boom.job.router;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import me.stevenkin.boom.job.common.dto.JobFireRequest;
import me.stevenkin.boom.job.common.service.ClientProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlacklistRouter implements Router {
    private URL url;

    public BlacklistRouter(URL url) {
        this.url = url;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Class<?> clazz = invokers.get(0).getInterface();
        String method = invocation.getMethodName();
        Class<?>[] types = invocation.getParameterTypes();
        if (!(ClientProcessor.class.equals(clazz)
                && "fireJob".equals(method) && types != null
                && types.length == 1 && JobFireRequest.class.equals(types[0])))
            return invokers;
        Object[] args = invocation.getArguments();
        JobFireRequest request = (JobFireRequest) args[0];
        List<String> blacklist = request.getBlacklist();
        if (blacklist == null || blacklist.isEmpty())
            return invokers;
        Set<String> blacklistSet = new HashSet<>(blacklist);
        return invokers.stream().filter(invoker -> !blacklistSet.contains(invoker.getUrl().getAddress())).collect(Collectors.toList());
    }

    @Override
    public int compareTo(Router o) {
        return 0;
    }
}
