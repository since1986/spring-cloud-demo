package com.github.since1986.demo.profile.service;

import com.github.since1986.demo.profile.mapper.ProfileMapper;
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

    @Autowired
    private ProfileMapper profileMapper;

    @Override
    public Profile get(long id) {
        LOGGER.info(id + "");
        LOGGER.error("test");
        LOGGER.info("test");
        LOGGER.trace("test");
        return profileMapper.get(id);
    }
}
