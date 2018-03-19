package com.github.since1986.demo.gateway.service;

import com.github.since1986.demo.gateway.model.RemoteCallEvent;
import com.github.since1986.demo.id.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class ProfileServiceImpl implements ProfileService {

    private Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final RemoteCallEventService remoteCallEventService;
    private final IdGenerator idGenerator;

    @Autowired
    public ProfileServiceImpl(RemoteCallEventService remoteCallEventService, IdGenerator idGenerator) {
        this.remoteCallEventService = remoteCallEventService;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(String email, String phone) { //TODO 可以用写一个"接口定义契约，实现由动态代理+注解自动生成"的这种方式通用化(参考Feign或@Transactional的实现方式)
        List<Class> remoteServiceMethodParamTypes = new ArrayList<>();
        remoteServiceMethodParamTypes.add(email.getClass());
        remoteServiceMethodParamTypes.add(phone.getClass());
        List<String> remoteServiceMethodParamValues = new ArrayList<>();
        remoteServiceMethodParamValues.add(email);
        remoteServiceMethodParamValues.add(phone);
        remoteCallEventService.save(
                RemoteCallEvent
                        .newBuilder()
                        .withId(idGenerator.nextId())
                        .withStatus(RemoteCallEvent.Status.CREATED)
                        .withTimestamp(System.currentTimeMillis())
                        .withRemoteServiceInterfaceName("com.github.since1986.demo.profile.service.ProfileService")
                        .withRemoteServiceSpringBeanName("profileService")
                        .withRemoteServiceMethodName("save")
                        .withRemoteServiceMethodParamTypes(remoteServiceMethodParamTypes)
                        .withRemoteServiceMethodParamValues(remoteServiceMethodParamValues)
                        .build()
        );
    }
}
