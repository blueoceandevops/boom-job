package me.stevenkin.boom.job.scheduler.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.cluster.ConfiguratorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.dubbo.common.Constants.*;

public class DubboServiceCache {
    private static final ConfiguratorFactory configuratorFactory = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class).getAdaptiveExtension();

    private Map<String, List<URL>> serviceUrlCache;

    private List<URL> configuredProviders;

    public DubboServiceCache() {
        serviceUrlCache = new HashMap<>();
        serviceUrlCache.put("providers", new ArrayList<>());
        serviceUrlCache.put("configurators", new ArrayList<>());

        configuredProviders = new ArrayList<>();
    }

    public synchronized void updateServiceCache(String category, List<URL> urls) {
        if (!serviceUrlCache.containsKey(category) || urls == null || urls.isEmpty()) {
            return;
        }
        List<URL> urlCache = serviceUrlCache.get(category);
        urlCache.clear();
        for (URL url : urls) {
            if (EMPTY_PROTOCOL.equals(url.getProtocol())) {
                urlCache.clear();
                break;
            }
            urlCache.add(url);
        }
        configProviders();
    }

    private void configProviders() {
        List<Configurator> localConfigurators = toConfigurator(serviceUrlCache.get("configurators"));
        configuredProviders.clear();
        if (localConfigurators != null) {
            for (URL providerUrl : serviceUrlCache.get("providers")) {
                for (Configurator configurator : localConfigurators) {
                    providerUrl = configurator.configure(providerUrl);
                }
                configuredProviders.add(providerUrl);
            }
        }
    }

    private List<Configurator> toConfigurator(List<URL> urls) {
        List<Configurator> result = new ArrayList<>();
        if (!(urls == null || urls.isEmpty()))
            urls.forEach(url -> result.add(configuratorFactory.getConfigurator(url)));
        return result;
    }

    public synchronized List<URL> getConfiguredProviders() {
        return new ArrayList<>(configuredProviders);
    }
}
