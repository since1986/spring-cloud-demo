package com.github.since1986.demo.gateway.task;

import com.github.since1986.demo.gateway.service.RemoteCallEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventPublishTask {

    private final RemoteCallEventService remoteCallEventService;

    @Autowired
    public EventPublishTask(RemoteCallEventService remoteCallEventService) {
        this.remoteCallEventService = remoteCallEventService;
    }

    @Scheduled(fixedRate = 1000)
    private void execute() {
        remoteCallEventService.publish();
    }
}
