package com.github.since1986.demo.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.profile.mapper.RemoteCallEventMapper;
import com.github.since1986.demo.profile.model.RemoteCallEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Transactional
@Service
public class RemoteCallEventServiceImpl implements RemoteCallEventService {

    private final RemoteCallEventMapper remoteCallEventMapper;
    private final ObjectMapper objectMapper;
    private Logger LOGGER = LoggerFactory.getLogger(RemoteCallEventServiceImpl.class);

    @Autowired
    public RemoteCallEventServiceImpl(RemoteCallEventMapper remoteCallEventMapper, ObjectMapper objectMapper) {
        this.remoteCallEventMapper = remoteCallEventMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void consume() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<RemoteCallEvent> events = remoteCallEventMapper.findByStatus(RemoteCallEvent.Status.RECEIVED);
        for (RemoteCallEvent event : events) {
            Object serviceBean = ContextLoader.getCurrentWebApplicationContext().getBean(event.getRemoteServiceSpringBeanName(), event.getRemoteServiceInterfaceName());

            List<Class> remoteServiceMethodParamTypes = event.getRemoteServiceMethodParamTypes();
            List<?> remoteServiceMethodParamValues = event.getRemoteServiceMethodParamValues();
            Method serviceMethod = serviceBean.getClass().getMethod(event.getRemoteServiceMethodName(), remoteServiceMethodParamTypes.toArray(new Class[0]));
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
}
