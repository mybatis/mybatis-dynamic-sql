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
    
    public static <T> List<T> selectList(Buildable<SelectModel> selectStatement, NamedParameterJdbcTemplate template,
            RowMapper<T> rowMapper) {
        return selectList(selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), template,
                rowMapper);
    }

    public static <T> List<T> selectList(SelectStatementProvider selectStatement, NamedParameterJdbcTemplate template,
            RowMapper<T> rowMapper) {
        return template.query(selectStatement.getSelectStatement(), selectStatement.getParameters(), rowMapper);
    }

    public static <T> Optional<T> selectOne(Buildable<SelectModel> selectStatement,
            NamedParameterJdbcTemplate template, RowMapper<T> rowMapper) {
        return selectOne(selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER),
                template, rowMapper);
    }
    
    public static <T> Optional<T> selectOne(SelectStatementProvider selectStatement,
            NamedParameterJdbcTemplate template, RowMapper<T> rowMapper) {
        try {
            return Optional.of(
                    template.queryForObject(selectStatement.getSelectStatement(), selectStatement.getParameters(),
                            rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    public static int delete(Buildable<DeleteModel> deleteStatement, NamedParameterJdbcTemplate template) {
        return delete(deleteStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), template);
    }
    
    public static int delete(DeleteStatementProvider deleteStatement, NamedParameterJdbcTemplate template) {
        return template.update(deleteStatement.getDeleteStatement(), deleteStatement.getParameters());
    }

    public static int generalInsert(Buildable<GeneralInsertModel> insertStatement,
            NamedParameterJdbcTemplate template) {
        return generalInsert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), template);
    }
    
    public static int generalInsert(GeneralInsertStatementProvider insertStatement,
            NamedParameterJdbcTemplate template) {
        return template.update(insertStatement.getInsertStatement(), insertStatement.getParameters());
    }

    public static <T> int insert(Buildable<InsertModel<T>> insertStatement, NamedParameterJdbcTemplate template) {
        return insert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), template);
    }
    
    public static <T> int insert(InsertStatementProvider<T> insertStatement, NamedParameterJdbcTemplate template) {
        return template.update(insertStatement.getInsertStatement(),
                new BeanPropertySqlParameterSource(insertStatement.getRecord()));
    }

    public static int update(Buildable<UpdateModel> updateStatement, NamedParameterJdbcTemplate template) {
        return update(updateStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), template);
    }
    
    public static int update(UpdateStatementProvider updateStatement, NamedParameterJdbcTemplate template) {
        return template.update(updateStatement.getUpdateStatement(), updateStatement.getParameters());
    }
}
