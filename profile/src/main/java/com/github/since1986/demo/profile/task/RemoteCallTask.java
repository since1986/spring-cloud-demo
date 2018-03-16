package com.github.since1986.demo.profile.task;

import com.github.since1986.demo.profile.service.RemoteCallEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
public class RemoteCallTask {

    private final RemoteCallEventService remoteCallEventService;

    @Autowired
    public RemoteCallTask(RemoteCallEventService remoteCallEventService) {
        this.remoteCallEventService = remoteCallEventService;
    }

    @Scheduled(fixedRate = 2000)
    public void call() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        remoteCallEventService.consume();
    }
}
