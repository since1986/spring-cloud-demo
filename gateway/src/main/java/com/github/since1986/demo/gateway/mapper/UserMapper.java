package com.github.since1986.demo.gateway.mapper;

import com.github.since1986.demo.gateway.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    void save(@Param("user") User user);

    User get(String username);
}
