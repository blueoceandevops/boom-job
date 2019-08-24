package me.stevenkin.boom.job.scheduler.dubbo;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DubboProviderScanner implements DisposableBean{

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DubboConfigHolder dubboConfigHolder;

    private Map<String, ServiceBean<Object>> serviceBeanMap = new HashMap<>();

    @PostConstruct
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
        serviceConfig.setApplication(dubboConfigHolder.getApplicationConfig());
        serviceConfig.setRegistry(dubboConfigHolder.getRegistryConfig());
        serviceConfig.setProtocol(dubboConfigHolder.getProtocolConfig());
        serviceConfig.setApplicationContext(this.applicationContext);
        try {
            serviceConfig.afterPropertiesSet();
            serviceConfig.setRef(bean);
            serviceBeanMap.put(beanName, serviceConfig);
            serviceConfig.export();
        } catch (Exception e) {
            log.error("scan provider happened error", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void destroy() throws Exception {
        serviceBeanMap.values().forEach(s -> {
            try {
                s.destroy();
            } catch (Exception e) {
                log.error("destroy service {} happened error", s, e);
            }
        });
        serviceBeanMap.clear();
    }
}
