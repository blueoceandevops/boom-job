package me.stevenkin.boom.job.scheduler.dubbo;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import lombok.extern.slf4j.Slf4j;
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

    private List<String> schedulerIds = new ArrayList<>();

    private String schedulerId;

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
            if (serviceName.equals(SERVICE_NAME)){
                schedulerIds.add(NameKit.getNodeId(serviceConfig.toUrl().toFullString()));
            }
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
        Assert.isTrue(schedulerIds.size() == 1, "schedulerId must be one");
        schedulerId = schedulerIds.get(0);
    }

    @Override
    public void doPause() throws Exception {

    }

    @Override
    public void doResume() throws Exception {

    }

    @Override
    public void doShutdown() throws Exception {
        //use dubbo jvm shutdown hook
    }
}
