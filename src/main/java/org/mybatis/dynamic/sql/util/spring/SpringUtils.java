/**
 *    Copyright 2016-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.spring;

import java.util.List;
import java.util.Optional;

import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SpringUtils {
    private SpringUtils() {}
    
    public static long count(NamedParameterJdbcTemplate template, Buildable<SelectModel> countStatement) {
        return count(template, countStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }
    
    public static long count(NamedParameterJdbcTemplate template, SelectStatementProvider countStatement) {
        return template.queryForObject(countStatement.getSelectStatement(), countStatement.getParameters(), Long.class);
    }
    
    public static int delete(NamedParameterJdbcTemplate template, Buildable<DeleteModel> deleteStatement) {
        return delete(template, deleteStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }
    
    public static int delete(NamedParameterJdbcTemplate template, DeleteStatementProvider deleteStatement) {
        return template.update(deleteStatement.getDeleteStatement(), deleteStatement.getParameters());
    }

    public static int generalInsert(NamedParameterJdbcTemplate template,
            Buildable<GeneralInsertModel> insertStatement) {
        return generalInsert(template, insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }
    
    public static int generalInsert(NamedParameterJdbcTemplate template, 
            GeneralInsertStatementProvider insertStatement) {
        return template.update(insertStatement.getInsertStatement(), insertStatement.getParameters());
    }

    public static <T> int insert(NamedParameterJdbcTemplate template, Buildable<InsertModel<T>> insertStatement) {
        return insert(template, insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }
    
    public static <T> int insert(NamedParameterJdbcTemplate template, InsertStatementProvider<T> insertStatement) {
        return template.update(insertStatement.getInsertStatement(),
                new BeanPropertySqlParameterSource(insertStatement.getRecord()));
    }

    public static <T> List<T> selectList(NamedParameterJdbcTemplate template, Buildable<SelectModel> selectStatement,
            RowMapper<T> rowMapper) {
        return selectList(template, selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER),
                rowMapper);
    }

    public static <T> List<T> selectList(NamedParameterJdbcTemplate template, SelectStatementProvider selectStatement,
            RowMapper<T> rowMapper) {
        return template.query(selectStatement.getSelectStatement(), selectStatement.getParameters(), rowMapper);
    }

    public static <T> Optional<T> selectOne(NamedParameterJdbcTemplate template, Buildable<SelectModel> selectStatement,
            RowMapper<T> rowMapper) {
        return selectOne(template, selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER),
                rowMapper);
    }
    
    public static <T> Optional<T> selectOne(NamedParameterJdbcTemplate template, SelectStatementProvider selectStatement,
            RowMapper<T> rowMapper) {
        T result;
        try {
            result = template.queryForObject(selectStatement.getSelectStatement(), selectStatement.getParameters(),
                            rowMapper);
        } catch (EmptyResultDataAccessException e) {
            result = null;
        }
        
        return Optional.ofNullable(result);
    }
    
    public static int update(NamedParameterJdbcTemplate template, Buildable<UpdateModel> updateStatement) {
        return update(template, updateStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }
    
    public static int update(NamedParameterJdbcTemplate template, UpdateStatementProvider updateStatement) {
        return template.update(updateStatement.getUpdateStatement(), updateStatement.getParameters());
    }
}
