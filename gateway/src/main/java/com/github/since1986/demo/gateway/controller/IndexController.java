package com.github.since1986.demo.gateway.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequestMapping("/private")
@RestController
public class IndexController {

    @PreAuthorize("hasAuthority('ROLE_SYSTEM')")
    @GetMapping("/time")
    public Date time() {
        return new Date();
    }
}
