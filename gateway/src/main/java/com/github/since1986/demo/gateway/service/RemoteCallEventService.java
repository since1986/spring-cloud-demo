package com.github.since1986.demo.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.since1986.demo.gateway.model.RemoteCallEvent;

public interface RemoteCallEventService {

    void save(RemoteCallEvent remoteCallEvent);

    void publish() throws JsonProcessingException;
}
