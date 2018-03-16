package com.github.since1986.demo.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.gateway.mapper.RemoteCallEventMapper;
import com.github.since1986.demo.gateway.model.RemoteCallEvent;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

@Transactional
@Service
public class RemoteCallEventServiceImpl implements RemoteCallEventService {

    private Logger LOGGER = LoggerFactory.getLogger(RemoteCallEventServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate kafkaTemplate;
    private final RemoteCallEventMapper remoteCallEventMapper;

    @Autowired
    public RemoteCallEventServiceImpl(RemoteCallEventMapper remoteCallEventMapper, KafkaTemplate kafkaTemplate, ObjectMapper objectMapper) {
        this.remoteCallEventMapper = remoteCallEventMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(RemoteCallEvent remoteCallEvent) {
        remoteCallEventMapper.save(remoteCallEvent);
    }

    @Override
    public void publish() throws JsonProcessingException {
        List<RemoteCallEvent> events = remoteCallEventMapper.findByStatus(RemoteCallEvent.Status.CREATED);
        for (RemoteCallEvent event : events) {
            ListenableFuture<SendResult<String, String>> sendResultListenableFuture =
                    kafkaTemplate.send(
                            event.getRemoteServiceInterfaceName() + event.getRemoteServiceMethodName(),
                            objectMapper.writeValueAsString(event)
                    );
            sendResultListenableFuture.addCallback(
                    result -> {
                        LOGGER.debug(result.getRecordMetadata().toString());
                        remoteCallEventMapper.updateStatus(event.getId(), RemoteCallEvent.Status.PUBLISHED);
                        LOGGER.debug(event.getId() + " publish success");
                    },
                    ex -> {
                        LOGGER.error(event.getId() + " publish failure");
                        throw new KafkaException(ex);
                    }
            );
        }
    }
}
