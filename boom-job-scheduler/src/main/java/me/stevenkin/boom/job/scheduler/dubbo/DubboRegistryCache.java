package me.stevenkin.boom.job.scheduler.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryService;
import me.stevenkin.boom.job.scheduler.cluster.RegistryListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DubboRegistryCache implements InitializingBean, DisposableBean, NotifyListener {
    private static final URL SUBSCRIBE = new URL(Constants.ADMIN_PROTOCOL, NetUtils.getLocalHost(), 0, "",
            Constants.INTERFACE_KEY, Constants.ANY_VALUE,
            Constants.GROUP_KEY, Constants.ANY_VALUE,
            Constants.VERSION_KEY, Constants.ANY_VALUE,
            Constants.CLASSIFIER_KEY, Constants.ANY_VALUE,
            Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY + ","
            + Constants.CONFIGURATORS_CATEGORY,
            Constants.ENABLED_KEY, Constants.ANY_VALUE,
            Constants.CHECK_KEY, String.valueOf(false));

    @Autowired
    private RegistryService registryService;

    private Map<String, DubboServiceCache> serviceCacheMap = new HashMap<>();

    private Map<String, List<RegistryListener>> registryListenerMap = new HashMap<>();

    @Override
    public synchronized void notify(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        String serviceInterface = urls.get(0).getServiceInterface();
        String category = urls.get(0).getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        DubboServiceCache cache = serviceCacheMap.get(serviceInterface);
        if (cache == null) {
            cache = new DubboServiceCache();
            serviceCacheMap.put(serviceInterface, cache);
        }
        cache.updateServiceCache(category, urls);
        DubboServiceCache cache1 = cache;
        List<RegistryListener> listeners = registryListenerMap.get(serviceInterface);
        if (listeners != null) {
            listeners.forEach(listener -> listener.update(cache1.getConfiguredProviders()));
        }
    }

    public synchronized void registerListener(String service, RegistryListener listener) {
        List<RegistryListener> listeners = registryListenerMap.get(service);
        if (listeners == null) {
            listeners = new ArrayList<>();
            registryListenerMap.put(service, listeners);
        }
        listeners.add(listener);
        DubboServiceCache cache = serviceCacheMap.get(service);
        if (cache != null) {
            listener.update(cache.getConfiguredProviders());
        }
    }

    public synchronized void unregisterListener(String service, RegistryListener listener) {
        List<RegistryListener> listeners = registryListenerMap.get(service);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public void destroy() throws Exception {
        registryService.unsubscribe(SUBSCRIBE, this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registryService.subscribe(SUBSCRIBE, this);
    }
}
