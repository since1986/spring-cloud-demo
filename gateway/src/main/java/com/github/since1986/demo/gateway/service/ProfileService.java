package com.github.since1986.demo.gateway.service;

import com.github.since1986.demo.gateway.model.Profile;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("comgithubsince1986demoprofile")
public interface ProfileService {

    void save(String email, String phone);

    @RequestMapping(method = RequestMethod.GET, value = "/profile/{username}")
    Profile get(@PathVariable String username);
}
