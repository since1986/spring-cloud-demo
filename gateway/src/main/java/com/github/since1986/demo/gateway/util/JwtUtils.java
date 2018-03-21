package com.github.since1986.demo.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.gateway.SecurityConfig;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.util.Arrays;
import java.util.Collections;

public class JwtUtils {

    public static Jwt genATestJwt() throws JsonProcessingException {
        SecurityConfig.PrivateWebSecurityConfigurationAdapter.JwtPayload jwtPayload = new SecurityConfig.PrivateWebSecurityConfigurationAdapter.JwtPayload();
        jwtPayload.setUsername("admin");
        jwtPayload.setAuthorities(Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
        return JwtHelper.encode(new ObjectMapper().writeValueAsString(jwtPayload), new MacSigner("123456"));
    }

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(genATestJwt().getEncoded());
    }
}
