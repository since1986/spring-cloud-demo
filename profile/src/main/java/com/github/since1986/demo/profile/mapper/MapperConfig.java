package com.github.since1986.demo.profile.mapper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan(basePackageClasses = MapperConfig.class)
@Configuration
public class MapperConfig {
}
