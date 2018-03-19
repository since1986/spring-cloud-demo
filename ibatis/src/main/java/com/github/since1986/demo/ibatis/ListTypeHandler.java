package com.github.since1986.demo.ibatis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public abstract class ListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    private Logger LOGGER = LoggerFactory.getLogger(ListTypeHandler.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            parameter.forEach((element) -> LOGGER.debug(element.toString()));
            try {
                LOGGER.debug(objectMapper.writeValueAsString(parameter));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage());
            }
        }
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}