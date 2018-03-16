package com.github.since1986.demo.profile.mapper;

import com.github.since1986.demo.profile.model.RemoteCallEvent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RemoteCallEventMapper {

    void save(@Param("event") RemoteCallEvent event);

    List<RemoteCallEvent> findByStatus(@Param("status") RemoteCallEvent.Status status);

    void updateStatus(@Param("id") long id, @Param("status") RemoteCallEvent.Status status);
}
