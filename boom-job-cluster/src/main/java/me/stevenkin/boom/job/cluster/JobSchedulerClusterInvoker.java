package me.stevenkin.boom.job.cluster;

import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.FailfastClusterInvoker;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.JobFireRequest;
import me.stevenkin.boom.job.common.dto.JobFireResponse;
import me.stevenkin.boom.job.common.enums.JobFireResult;
import me.stevenkin.boom.job.common.service.ClientProcessor;
import me.stevenkin.boom.job.common.service.JobSchedulerService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JobSchedulerClusterInvoker<T> extends FailfastClusterInvoker<T> {
    public JobSchedulerClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    public Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        checkInvokers(invokers, invocation);
        Class<?> clazz = invokers.get(0).getInterface();
        String method = invocation.getMethodName();
        Class<?>[] types = invocation.getParameterTypes();

        List<Result> results = new ArrayList<>();
        List<Throwable> throwables = new ArrayList<>();
        int size = invokers.size();

        if (isScheduler(clazz, method, types)) {
            for (Invoker<T> invoker : invokers) {
                try {
                    results.add(invoker.invoke(invocation));
                }catch (Throwable e) {
                    throwables.add(e);
                }
            }
            if (throwables.size() == size) {
                log.error("all provider is not alive " + loadbalance.getClass().getSimpleName() + " select from all providers " + invokers + " for service " + getInterface().getName() + " method " + invocation.getMethodName() + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", but no luck to perform the invocation. Last error is: " + throwables.get(throwables.size() - 1).getMessage(), throwables.get(throwables.size() - 1).getCause() != null ? throwables.get(throwables.size() - 1).getCause() : throwables.get(throwables.size() - 1));
                return new RpcResult(Boolean.FALSE);
            }
            List<Result> results1 = results.stream().filter(r -> Boolean.TRUE.equals(r.getValue())).collect(Collectors.toList());
            if (results1.size() > 1) {
                log.error("a biz exception happen, select from all providers " + invokers + " for service " + getInterface().getName() + " method " + invocation.getMethodName() + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + " has multiple success, but only allow one success");
                return new RpcResult(Boolean.FALSE);
            }
            if (results1.size() == 1) {
                return new RpcResult(Boolean.TRUE);
            }
            return new RpcResult(Boolean.FALSE);
        }
        if (isProcessor(clazz, method, types)) {
            JobFireRequest request = (JobFireRequest) invocation.getArguments()[0];
            List<Long> jobShardIds = request.getJobShardIds();
            if (jobShardIds != null && !jobShardIds.isEmpty()) {
                int i = 0;
                for (Long id : jobShardIds) {
                    JobFireRequest request1 = new JobFireRequest();
                    request1.setJobKey(request.getJobKey());
                    request1.setSchedulerId(request.getSchedulerId());
                    request1.setJobInstanceId(request.getJobInstanceId());
                    List<Long> ids = new ArrayList<>();
                    ids.add(id);
                    request1.setJobShardIds(ids);
                    try {
                        results.add(invokers.get(i % invokers.size()).invoke(invocation));
                    }catch (Throwable e) {
                        throwables.add(e);
                    }
                }
                if (size > jobShardIds.size()) {
                    for (int j = jobShardIds.size(); j < size; j++) {
                        JobFireRequest request2 = new JobFireRequest();
                        request2.setJobKey(request.getJobKey());
                        request2.setSchedulerId(request.getSchedulerId());
                        request2.setJobInstanceId(request.getJobInstanceId());
                        request2.setJobShardIds(new ArrayList<>());
                        try {
                            results.add(invokers.get(i % invokers.size()).invoke(invocation));
                        }catch (Throwable e) {
                            throwables.add(e);
                        }
                    }
                }
                if (throwables.size() == size) {
                    log.error("all provider is not alive, select from all providers " + invokers + " for service " + getInterface().getName() + " method " + invocation.getMethodName() + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", but no luck to perform the invocation. Last error is: " + throwables.get(throwables.size() - 1).getMessage(), throwables.get(throwables.size() - 1).getCause() != null ? throwables.get(throwables.size() - 1).getCause() : throwables.get(throwables.size() - 1));
                    return new RpcResult(new JobFireResponse(JobFireResult.FIRE_FAILED, null));
                }
                boolean isFailed = results.stream().anyMatch(r -> JobFireResult.FIRE_FAILED.equals(((JobFireResponse)(r.getValue())).getJobFireResult()));
                if (isFailed) {
                    log.error("a biz exception happen, select from all providers " + invokers + " for service " + getInterface().getName() + " method " + invocation.getMethodName() + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + " one provider return failed");
                    return new RpcResult(new JobFireResponse(JobFireResult.FIRE_FAILED, null));
                }
                List<String> clients = new ArrayList<>();
                results.stream().filter(r -> JobFireResult.FIRE_SUCCESS.equals(((JobFireResponse)(r.getValue()))))
                        .map(r -> (List<String>)r.getValue())
                        .forEach(clients::addAll);
                return new RpcResult(new JobFireResponse(JobFireResult.FIRE_SUCCESS, clients));
            }
            return new RpcResult(new JobFireResponse(JobFireResult.FIRE_FAILED, null));
        }
        return super.doInvoke(invocation, invokers, loadbalance);
    }

    private boolean isScheduler(Class<?> clazz, String method, Class<?>[] types) {
        return clazz.equals(JobSchedulerService.class);
    }

    private boolean isProcessor(Class<?> clazz, String method, Class<?>[] types) {
        return clazz.equals(ClientProcessor.class)
                && method.equals("fireJob")
                && types != null
                && types.length == 1
                && types[0].equals(JobFireRequest.class);
    }
}
