package me.stevenkin.boom.job.scheduler.cluster;

import com.alibaba.dubbo.common.URL;

import java.util.List;

/**
 * push urls to cluster
 */
@FunctionalInterface
public interface RegistryListener {

    void update(List<URL> urls);
}
