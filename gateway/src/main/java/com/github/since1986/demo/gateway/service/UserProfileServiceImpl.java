package com.github.since1986.demo.gateway.service;

import com.github.since1986.demo.gateway.mapper.UserMapper;
import com.github.since1986.demo.gateway.model.Profile;
import com.github.since1986.demo.gateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Transactional
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final ProfileService profileService;

    private final UserMapper userMapper;

    @Autowired
    public UserProfileServiceImpl(UserMapper userMapper, ProfileService profileService) {
        this.userMapper = userMapper;
        this.profileService = profileService;
    }

    @Override
    public Map<String, Object> get(String username) {
        User user = userMapper.get(username);
        Profile profile = profileService.get(username);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("name", profile.getName());
        result.put("email", profile.getEmail());
        result.put("phone", profile.getPhone());
        return result;
    }
}
