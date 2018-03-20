package com.github.since1986.demo.profile.service;

import com.github.since1986.demo.profile.model.Profile;

public interface ProfileService {

    Profile get(String username);

    void save(Profile profile);

    void save(String email, String phone);
}
