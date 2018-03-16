package com.github.since1986.demo.gateway.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.gateway.annotation.EventPayload;
import com.github.since1986.demo.gateway.model.RemoteCallEvent;
import com.github.since1986.demo.gateway.service.RemoteCallEventService;
import com.github.since1986.demo.id.IdGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//所有需要分布一致性的过程全部由此处理
@Aspect
@Component
public class DistributedConsistencyAspect {

    private final IdGenerator idGenerator;
    private final RemoteCallEventService remoteCallEventService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DistributedConsistencyAspect(RemoteCallEventService remoteCallEventService, IdGenerator idGenerator, ObjectMapper objectMapper) {
        this.remoteCallEventService = remoteCallEventService;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Pointcut("@annotation(com.github.since1986.demo.gateway.annotation.DistributedConsistency)")
    public void pointcut() {
    }


    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object payload = null;
        for (Object arg : proceedingJoinPoint.getArgs()) {
            if (arg != null && arg.getClass().getDeclaredAnnotation(EventPayload.class) != null) {
                payload = arg;
            }
        }
        remoteCallEventService.save(
                RemoteCallEvent
                        .newBuilder()
                        .withId(idGenerator.nextId())
                        .withStatus(RemoteCallEvent.Status.CREATED)
                        .withTimestamp(System.currentTimeMillis())
                        .build()

        );
        return proceedingJoinPoint.proceed();
    }
}
