package me.stevenkin.boom.job.scheduler.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.registry.RegistryService;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dubbo.Configuration;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.support.Lifecycle;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DubboProviderScanner extends Lifecycle {
    private static final String SERVICE_NAME = "me.stevenkin.boom.job.common.service.JobSchedulerService";

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DubboConfigHolder dubboConfigHolder;
    @Autowired
    private RegistryService registryService;

    private List<String> schedulerIds = new ArrayList<>();

    private String schedulerId;

    private List<URL> providerUrls = new ArrayList<>();

    private List<URL> configUrls = new ArrayList<>();

    private Map<String, ServiceBean<Object>> serviceBeanMap = new HashMap<>();

    public void scan() {
        Map<String, Object> beanMap = this.applicationContext.getBeansWithAnnotation(Service.class);
        if (beanMap != null && !beanMap.isEmpty()) {
            beanMap.forEach(this::bean2Service);
        }
    }

    private void bean2Service(String beanName, Object bean) {
        Service service = this.applicationContext.findAnnotationOnBean(beanName, Service.class);
        Assert.isTrue(service != null, beanName + "have not Service annotation");
        ServiceBean<Object> serviceConfig = new ServiceBean<Object>(service);
        if (StringUtils.isEmpty(serviceConfig.getInterface()) &&
                (serviceConfig.getInterfaceClass() == null || serviceConfig.getInterfaceClass().equals(void.class))) {
            Class<?>[] interfaceClazzs;
            if (AopUtils.isAopProxy(bean)) {
                interfaceClazzs = AopUtils.getTargetClass(bean).getInterfaces();
            }else {
                interfaceClazzs = bean.getClass().getInterfaces();
            }
            if (ArrayUtils.isNotEmpty(interfaceClazzs)) {
                serviceConfig.setInterface(interfaceClazzs[0]);
            }
        }
        String serviceName = null;
        if (!StringUtils.isEmpty(serviceConfig.getInterface())) {
            serviceName = serviceConfig.getInterface();
        }
        if (StringUtils.isEmpty(serviceName)) {
            serviceName = serviceConfig.getInterfaceClass().getCanonicalName();
        }
        Assert.isTrue(!StringUtils.isEmpty(serviceName), "service interface name must be not empty");
        serviceConfig.setApplication(dubboConfigHolder.getApplicationConfig());
        serviceConfig.setRegistry(dubboConfigHolder.getRegistryConfig());
        serviceConfig.setProtocol(dubboConfigHolder.getProtocolConfig());
        serviceConfig.setApplicationContext(this.applicationContext);
        try {
            serviceConfig.afterPropertiesSet();
            serviceConfig.setRef(bean);
            serviceBeanMap.put(beanName, serviceConfig);
            serviceConfig.export();
            providerUrls.add(serviceConfig.toUrl());
        } catch (Exception e) {
            log.error("scan provider happened error", e);
            throw new RuntimeException(e);
        }

    }

    public String getSchedulerId() {
        return schedulerId;
    }

    @Override
    public void doStart() throws Exception {
        scan();
        findSchedulerId();
    }

    private void findSchedulerId() {
        Assert.isTrue(!providerUrls.isEmpty(), "provider urls must not be empty");
        for (URL url : providerUrls) {
            if (SERVICE_NAME.equals(url.getServiceInterface())) {
                schedulerId = NameKit.getNodeId(url.toFullString());
                return;
            }
        }
        throw new RuntimeException("can not find the scheduler id");
    }

    @Override
    public void doPause() throws Exception {
        configUrls.clear();
        for (URL url : providerUrls) {
            Configuration configuration = new Configuration();
            configuration.setService(url.getServiceInterface());
            configuration.setAddress(url.getAddress());
            configuration.setPort(url.getPort());
            configuration.setEnabled(true);
            URL url1 = configuration.toUrl();
            configUrls.add(url1);
            registryService.register(url1);
        }
    }

    @Override
    public void doResume() throws Exception {
        Assert.isTrue(!configUrls.isEmpty() && configUrls.size() == providerUrls.size(), "config urls must not be empty");
        for (URL url : configUrls) {
            registryService.unregister(url);
        }
        configUrls.clear();
    }

    @Override
    public void doShutdown() throws Exception {
        //use dubbo jvm shutdown hook
    }
}
