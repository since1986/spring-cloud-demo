package com.github.since1986.demo.gateway.service;

import com.github.since1986.demo.gateway.model.Profile;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("comgithubsince1986demoprofile")
public interface ProfileService {

    @RequestMapping(method = RequestMethod.POST, value = "/profile")
    void save(@RequestParam("email") String email, @RequestParam("phone") String phone);

    @RequestMapping(method = RequestMethod.GET, value = "/profile/{username}")
    Profile get(@PathVariable("username") String username);
}
