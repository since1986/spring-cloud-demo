package com.github.since1986.demo.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Configuration
public class MyBatisConfig {

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
