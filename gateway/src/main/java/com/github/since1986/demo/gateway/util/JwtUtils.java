package com.github.since1986.demo.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    public static Jwt genATestJwt() throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "test");
        payload.put("expirationTime", LocalDateTime.of(2018, 12, 1, 12, 30, 30).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        payload.put("authorities", new String[]{"ROLE_ADMIN", "ROLE_USER"});
        return JwtHelper.encode(new ObjectMapper().writeValueAsString(payload), new MacSigner("123456"));
    }

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(genATestJwt().getEncoded());
    }
}
