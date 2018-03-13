package com.github.since1986.demo.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.since1986.demo.gateway.SecurityConfig;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.util.Collections;

public class JwtUtils {

    public static Jwt genATestJwt() throws JsonProcessingException {
        SecurityConfig.PrivateWebSecurityConfigurationAdapter.JwtUserDetailPayload jwtUserDetailPayload = new SecurityConfig.PrivateWebSecurityConfigurationAdapter.JwtUserDetailPayload();
        jwtUserDetailPayload.setAud("testUser");
        jwtUserDetailPayload.setScopes(Collections.singletonList("ROLE_USER"));
        return JwtHelper.encode(new ObjectMapper().writeValueAsString(jwtUserDetailPayload), new MacSigner("123456"));
    }

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(genATestJwt().getEncoded());
    }
}
