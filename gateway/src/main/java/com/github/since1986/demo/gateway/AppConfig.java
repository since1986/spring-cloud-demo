package com.github.since1986.demo.gateway;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.since1986.demo.id.IdGenerator;
import com.github.since1986.demo.id.SnowflakeIdGenerator;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;

@EnableScheduling
@EnableAspectJAutoProxy
@EnableTransactionManagement(proxyTargetClass = true)
@Configuration
public class AppConfig {

    private final AppProperties appProperties;

    @Autowired
    public AppConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public DataSource dataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(appProperties.getDataSource().getDriverClassName());
        basicDataSource.setUrl(appProperties.getDataSource().getUrl());
        basicDataSource.setUsername(appProperties.getDataSource().getUsername());
        basicDataSource.setPassword(appProperties.getDataSource().getPassword());
        return basicDataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource());
        return dataSourceTransactionManager;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper
                .getSerializerProvider()
                .setNullValueSerializer(new JsonSerializer<Object>() {
                    @Override
                    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        gen.writeString("");  //null 转 ""
                    }
                });
        return objectMapper;
    }

    @Bean
    public IdGenerator idGenerator() {
        return new SnowflakeIdGenerator(0, 1);
    }
}
