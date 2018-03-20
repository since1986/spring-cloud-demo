package com.github.since1986.demo.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.profile.mapper.RemoteCallEventMapper;
import com.github.since1986.demo.profile.model.RemoteCallEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

@Transactional
@Service
public class RemoteCallEventServiceImpl implements RemoteCallEventService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final RemoteCallEventMapper remoteCallEventMapper;
    private final ObjectMapper objectMapper;
    private Logger LOGGER = LoggerFactory.getLogger(RemoteCallEventServiceImpl.class);

    @Autowired
    public RemoteCallEventServiceImpl(RemoteCallEventMapper remoteCallEventMapper, ObjectMapper objectMapper) {
        this.remoteCallEventMapper = remoteCallEventMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void consume() throws Throwable {
        List<RemoteCallEvent> events = remoteCallEventMapper.findByStatus(RemoteCallEvent.Status.RECEIVED);
        for (RemoteCallEvent event : events) {
            String serviceBeanName = event.getRemoteServiceSpringBeanName();
            Class<?> serviceInterfaceClass = Class.forName(event.getRemoteServiceInterfaceName());
            List<Class> serviceMethodParamTypes = event.getRemoteServiceMethodParamTypes();
            List<?> serviceMethodParamValues = event.getRemoteServiceMethodParamValues();
            String serviceMethodName = event.getRemoteServiceMethodName();
            invokeBeanMethod(serviceBeanName, serviceInterfaceClass, serviceMethodName, serviceMethodParamTypes, serviceMethodParamValues);
            remoteCallEventMapper.updateStatus(event.getId(), RemoteCallEvent.Status.CONSUMED);
        }
    }

    @Override
    public void save(String json) throws IOException {
        RemoteCallEvent event = objectMapper.readValue(json, RemoteCallEvent.class);
        event.setStatus(RemoteCallEvent.Status.RECEIVED);
        remoteCallEventMapper.save(event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void invokeBeanMethod(String beanName, Class<?> beanClass, String methodName, List<Class> methodParamTypes, List<?> methodParamValues) throws Throwable {
        Object bean;
        if (StringUtils.isNoneBlank(beanName)) {
            bean = applicationContext.getBean(beanName, beanClass);
        } else {
            bean = applicationContext.getBean(beanClass);
        }
        Method method = beanClass.getMethod(methodName, methodParamTypes.toArray(new Class[0]));
        AopUtils.invokeJoinpointUsingReflection(bean, method, methodParamValues.toArray());
    }
}
