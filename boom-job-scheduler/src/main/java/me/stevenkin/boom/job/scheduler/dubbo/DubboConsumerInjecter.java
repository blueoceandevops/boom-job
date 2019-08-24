package me.stevenkin.boom.job.scheduler.dubbo;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DubboConsumerInjecter implements BeanPostProcessor{

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DubboConfigHolder dubboConfigHolder;

    private Map<ClassIdBean, Object> referenceMap = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        Class<?> objClz;
        if (AopUtils.isAopProxy(bean)) {
            objClz = AopUtils.getTargetClass(bean);
        } else {
            objClz = bean.getClass();
        }

        try {
            for (Field field : objClz.getDeclaredFields()) {
                Reference reference = field.getAnnotation(Reference.class);
                ReferenceBean<?> referenceBean =
                        this.getConsumerBean(beanName, field, reference);
                Class<?> interfaceClass = referenceBean.getInterfaceClass();
                String group = referenceBean.getGroup();
                String version = referenceBean.getVersion();
                ClassIdBean classIdBean = new ClassIdBean(interfaceClass, group, version);
                Object dubboReference =
                        referenceMap.get(classIdBean);
                if (dubboReference == null) {
                    synchronized (this) {
                        // double check
                        dubboReference = referenceMap.get(classIdBean);
                        if (dubboReference == null) {
                            referenceBean.afterPropertiesSet();
                            // dubboReference should not be null, otherwise it will cause
                            // NullPointerException
                            dubboReference = referenceBean.getObject();
                            referenceMap.put(classIdBean,
                                    dubboReference);
                        }
                    }
                }
                field.setAccessible(true);
                field.set(bean, dubboReference);
            }
        } catch (Exception e) {
            throw new BeanCreationException(beanName, e);
        }
        return bean;
    }

    private <T> ReferenceBean<T> getConsumerBean(String beanName, Field field, Reference reference) throws BeansException {
        ReferenceBean<T> referenceBean = new ReferenceBean<>(reference);
        if ((reference.interfaceClass() == null || reference.interfaceClass() == void.class)
                && StringUtils.isEmpty(reference.interfaceName())) {
            referenceBean.setInterface(field.getType());
        }
        referenceBean.setApplicationContext(applicationContext);

        referenceBean.setApplication(dubboConfigHolder.getApplicationConfig());
        referenceBean.setRegistry(dubboConfigHolder.getRegistryConfig());
        referenceBean.setProtocol(dubboConfigHolder.getProtocolConfig().getName());
        referenceBean.setConsumer(dubboConfigHolder.getConsumerConfig());
        return referenceBean;
    }

}
