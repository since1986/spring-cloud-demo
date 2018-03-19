package com.github.since1986.demo.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.profile.mapper.RemoteCallEventMapper;
import com.github.since1986.demo.profile.model.RemoteCallEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
    public void consume() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        List<RemoteCallEvent> events = remoteCallEventMapper.findByStatus(RemoteCallEvent.Status.RECEIVED);
        for (RemoteCallEvent event : events) {
            Object serviceBean = null;
            if (StringUtils.isNoneBlank(event.getRemoteServiceSpringBeanName())) {
                serviceBean = applicationContext.getBean(event.getRemoteServiceSpringBeanName(), Class.forName(event.getRemoteServiceInterfaceName()));
            } else {
                serviceBean = applicationContext.getBean(Class.forName(event.getRemoteServiceInterfaceName()));
            }
            LOGGER.debug(serviceBean.toString());
            List<Class> remoteServiceMethodParamTypes = event.getRemoteServiceMethodParamTypes();
            List<?> remoteServiceMethodParamValues = event.getRemoteServiceMethodParamValues();
            Method serviceMethod = serviceBean.getClass().getMethod(event.getRemoteServiceMethodName(), remoteServiceMethodParamTypes.toArray(new Class[0]));
            //TODO CGLIB 反射获得方法对象
            serviceMethod.invoke(serviceBean, remoteServiceMethodParamValues.toArray(new Object[0]));
            LOGGER.debug(serviceMethod.toGenericString());
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
}
