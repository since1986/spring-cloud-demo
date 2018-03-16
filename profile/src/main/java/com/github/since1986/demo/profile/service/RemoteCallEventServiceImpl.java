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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            Map<String, String> remoteServiceMethodParamTypeValueMap = event.getRemoteServiceMethodParamTypeValueMap();
            List<Class<?>> paramTypes = new ArrayList<>();
            List<Object> param = new ArrayList<>();
            remoteServiceMethodParamTypeValueMap.forEach((paramType, paramValue) -> {
                try {
                    paramTypes.add(Class.forName(paramType));
                    param.add(objectMapper.readValue(paramValue, Class.forName(paramType)));
                } catch (ClassNotFoundException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
            Method serviceMethod = serviceBean.getClass().getMethod(event.getRemoteServiceMethodName(), paramTypes.toArray(new Class<?>[0]));
            serviceMethod.invoke(serviceBean, param.toArray(new Object[0]));
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
