package com.github.since1986.demo.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Configuration
public class MyBatisConfig {

    private final DataSource dataSource;

    @Autowired
    public MyBatisConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @MappedJdbcTypes(JdbcType.VARCHAR)
    @MappedTypes(Map.class)
    public class MapTypeHandler extends BaseTypeHandler<Map<String, String>> {

        private ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, Map<String, String> parameter, JdbcType jdbcType) throws SQLException {
            try {
                ps.setString(i, objectMapper.writeValueAsString(parameter));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<String, String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
            try {
                return objectMapper.readValue(rs.getString(columnName), new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<String, String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            try {
                return objectMapper.readValue(rs.getString(columnIndex), new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<String, String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            try {
                return objectMapper.readValue(cs.getString(columnIndex), new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
