package com.github.since1986.demo.gateway.service;

import com.github.since1986.demo.gateway.model.RemoteCallEvent;
import com.github.since1986.demo.id.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Transactional
@Service
public class ProfileServiceImpl implements ProfileService {

    private final RemoteCallEventService remoteCallEventService;
    private final IdGenerator idGenerator;

    @Autowired
    public ProfileServiceImpl(RemoteCallEventService remoteCallEventService, IdGenerator idGenerator) {
        this.remoteCallEventService = remoteCallEventService;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(String name, String gender) { //TODO 可以用写一个"接口定义契约，实现由动态代理+注解自动生成"的这种方式通用化(参考Feign或@Transactional的实现方式)
        Map<String, String> remoteServiceMethodParamTypeValueMap = new HashMap<>();
        remoteServiceMethodParamTypeValueMap.put(name.getClass().getName(), name);
        remoteServiceMethodParamTypeValueMap.put(gender.getClass().getName(), gender);
        remoteCallEventService.save(
                RemoteCallEvent
                        .newBuilder()
                        .withId(idGenerator.nextId())
                        .withStatus(RemoteCallEvent.Status.CREATED)
                        .withTimestamp(System.currentTimeMillis())
                        .withRemoteServiceInterfaceName("com.github.since1986.demo.profile.service.ProfileService")
                        .withRemoteServiceSpringBeanName("profileService")
                        .withRemoteServiceMethodName("save")
                        .withRemoteServiceMethodParamTypeValueMap(remoteServiceMethodParamTypeValueMap)
                        .build()
        );
    }
}
