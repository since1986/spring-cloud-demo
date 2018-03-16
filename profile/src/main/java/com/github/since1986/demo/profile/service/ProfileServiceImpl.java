package com.github.since1986.demo.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.profile.mapper.ProfileMapper;
import com.github.since1986.demo.profile.mapper.RemoteCallEventMapper;
import com.github.since1986.demo.profile.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final ProfileMapper profileMapper;

    private final RemoteCallEventMapper remoteCallEventMapper;

    private final ObjectMapper objectMapper;

    @Autowired
    public ProfileServiceImpl(ProfileMapper profileMapper, ObjectMapper objectMapper, RemoteCallEventMapper remoteCallEventMapper) {
        this.profileMapper = profileMapper;
        this.objectMapper = objectMapper;
        this.remoteCallEventMapper = remoteCallEventMapper;
    }

    @Override
    public Profile get(long id) {
        return profileMapper.get(id);
    }


    @Override
    public void save(Profile profile) {
        profileMapper.save(profile);
    }
}
