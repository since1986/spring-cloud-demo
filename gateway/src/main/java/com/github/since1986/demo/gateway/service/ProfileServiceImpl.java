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
        //TODO 可以这样处理：
        //1.将远程Service的接口以及远程定义的Model按照原包层级拷贝到本地工程中，在拷贝过来远程Service上加上@RemoteService注解以标识为远程接口
        //2.实现一个注解处理的类，通过反射拿到标识了@RemoteService的接口的InterfaceName以及MethodName信息（其他扩展信息定义在@RemoteService的属性中，可定义className、beanName）
        //3.将拿到的InterfaceName以及MethodName信息填入以下流程，这样就是声明式了，而不是硬编码了
        //4.提供一个底层的编程式调用接口，以方便更灵活的调用 RemoteServiceManager.invoke(RemoteServiceDefinition remoteServiceDefinition);
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
                        .withRemoteServiceMethodName("save")
                        .withRemoteServiceMethodParamTypes(remoteServiceMethodParamTypes)
                        .withRemoteServiceMethodParamValues(remoteServiceMethodParamValues)
                        .build()
        );
    }
}
