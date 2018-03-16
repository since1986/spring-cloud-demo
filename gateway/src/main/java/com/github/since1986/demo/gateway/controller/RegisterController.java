package com.github.since1986.demo.gateway.controller;

import com.github.since1986.demo.gateway.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/register")
@RestController
public class RegisterController {

    private final AccountService accountService;

    @Autowired
    public RegisterController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity execute(String username, String password, String name, String gender) {
        accountService.register(username, password, name, gender);
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
