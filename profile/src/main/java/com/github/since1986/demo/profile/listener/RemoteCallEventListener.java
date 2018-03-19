package com.github.since1986.demo.profile.listener;

import com.github.since1986.demo.profile.service.RemoteCallEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RemoteCallEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(RemoteCallEventListener.class);

    private final RemoteCallEventService remoteCallEventService;

    @Autowired
    public RemoteCallEventListener(RemoteCallEventService remoteCallEventService) {
        this.remoteCallEventService = remoteCallEventService;
    }

    @KafkaListener(topics = "PROFILE_SERVICE")
    public void listen(String payload) throws IOException {
        LOGGER.debug("event received: " + payload);
        remoteCallEventService.save(payload);
    }
}
