package com.ruyuan.careerplan.cookbook.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruyuan.careerplan.cookbook.domain.dto.Food;
import com.ruyuan.careerplan.cookbook.domain.dto.StepDetail;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhonghuashishan
 */
@MappedTypes(value = {StepDetail.class, Food.class})
@MappedJdbcTypes(value = {JdbcType.VARCHAR}, includeNullJdbcType = true)
public class ArrayListTypeHandler<T extends Object> extends BaseTypeHandler<List<T>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Class<List<T>> clazz;

    public ArrayListTypeHandler(Class<List<T>> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.clazz = clazz;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> ts, JdbcType jdbcType) throws SQLException {
        ps.setString(i, toJson(ts));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String s) throws SQLException {
        return toObject(rs.getString(s), List.class);
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int i) throws SQLException {
        return toObject(rs.getString(i), List.class);
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int i) throws SQLException {
        return toObject(cs.getString(i), List.class);
    }

    private String toJson(List<T> list) {
        try {
            return MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<T> toObject(String content, Class<?> clazz) {
        if (StringUtils.hasLength(content)) {
            try {
                return (List<T>) MAPPER.readValue(content, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
