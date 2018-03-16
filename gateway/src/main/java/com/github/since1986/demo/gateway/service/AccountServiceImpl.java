package com.github.since1986.demo.gateway.service;

import com.github.since1986.demo.gateway.mapper.AuthorityMapper;
import com.github.since1986.demo.gateway.mapper.UserMapper;
import com.github.since1986.demo.gateway.model.Authority;
import com.github.since1986.demo.gateway.model.User;
import com.github.since1986.demo.id.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class AccountServiceImpl implements AccountService {

    private final UserMapper userMapper;

    private final AuthorityMapper authorityMapper;

    private final PasswordEncoder passwordEncoder;

    private final IdGenerator idGenerator;

    private final ProfileService profileService;

    @Autowired
    public AccountServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, IdGenerator idGenerator, AuthorityMapper authorityMapper, ProfileService profileService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
        this.authorityMapper = authorityMapper;
        this.profileService = profileService;
    }

    @Override
    public void register(String username, String password, String name, String gender) {
        long userId = idGenerator.nextId();
        userMapper.save(
                User
                        .newBuilder()
                        .withId(userId)
                        .withUsername(username)
                        .withPassword(passwordEncoder.encode(password))
                        .withEnabled(true)
                        .build()
        );
        authorityMapper.save(
                Authority
                        .newBuilder()
                        .withId(idGenerator.nextId())
                        .withAuthority("ROLE_USER")
                        .withUserId(userId)
                        .withUsername(username)
                        .build()
        );
        profileService.save(name, gender);
    }
}
