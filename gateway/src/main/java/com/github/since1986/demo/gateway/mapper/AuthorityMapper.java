package com.github.since1986.demo.gateway.mapper;

import com.github.since1986.demo.gateway.model.Authority;
import org.apache.ibatis.annotations.Param;

public interface AuthorityMapper {

    void save(@Param("authority") Authority authority);
}
