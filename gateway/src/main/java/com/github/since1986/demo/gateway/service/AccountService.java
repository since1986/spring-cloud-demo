package com.github.since1986.demo.gateway.service;

import com.github.since1986.demo.gateway.model.Profile;

public interface AccountService {

    void register(String username, String password, String name, String gender);
}
