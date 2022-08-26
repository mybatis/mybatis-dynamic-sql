/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.spring;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.KeyHolder;

public class NamedParameterJdbcTemplateExtensions {
    private final NamedParameterJdbcTemplate template;

    public NamedParameterJdbcTemplateExtensions(NamedParameterJdbcTemplate template) {
        this.template = Objects.requireNonNull(template);
    }

    public long count(Buildable<SelectModel> countStatement) {
        return count(countStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }

    public long count(SelectStatementProvider countStatement) {
        return template.queryForObject(countStatement.getSelectStatement(), countStatement.getParameters(), Long.class);
    }

    public int delete(Buildable<DeleteModel> deleteStatement) {
        return delete(deleteStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }

    public int delete(DeleteStatementProvider deleteStatement) {
        return template.update(deleteStatement.getDeleteStatement(), deleteStatement.getParameters());
    }

    public int generalInsert(Buildable<GeneralInsertModel> insertStatement) {
        return generalInsert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }

    public int generalInsert(GeneralInsertStatementProvider insertStatement) {
        return template.update(insertStatement.getInsertStatement(), insertStatement.getParameters());
    }

    public int generalInsert(Buildable<GeneralInsertModel> insertStatement, KeyHolder keyHolder) {
        return generalInsert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), keyHolder);
    }

    public int generalInsert(GeneralInsertStatementProvider insertStatement, KeyHolder keyHolder) {
        return template.update(insertStatement.getInsertStatement(),
                new MapSqlParameterSource(insertStatement.getParameters()), keyHolder);
    }

    public <T> int insert(Buildable<InsertModel<T>> insertStatement) {
        return insert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }

    public <T> int insert(InsertStatementProvider<T> insertStatement) {
        return template.update(insertStatement.getInsertStatement(),
                new BeanPropertySqlParameterSource(insertStatement.getRow()));
    }

    public <T> int insert(Buildable<InsertModel<T>> insertStatement, KeyHolder keyHolder) {
        return insert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), keyHolder);
    }

    public <T> int insert(InsertStatementProvider<T> insertStatement, KeyHolder keyHolder) {
        return template.update(insertStatement.getInsertStatement(),
                new BeanPropertySqlParameterSource(insertStatement.getRow()), keyHolder);
    }

    public <T> int[] insertBatch(Buildable<BatchInsertModel<T>> insertStatement) {
        return insertBatch(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }

    public <T> int[] insertBatch(BatchInsert<T> insertStatement) {
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(insertStatement.getRecords());
        return template.batchUpdate(insertStatement.getInsertStatementSQL(), batch);
    }

    public <T> int insertMultiple(Buildable<MultiRowInsertModel<T>> insertStatement) {
        return insertMultiple(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }

    public <T> int insertMultiple(MultiRowInsertStatementProvider<T> insertStatement) {
        return template.update(insertStatement.getInsertStatement(),
                new BeanPropertySqlParameterSource(insertStatement));
    }

    public <T> int insertMultiple(Buildable<MultiRowInsertModel<T>> insertStatement, KeyHolder keyHolder) {
        return insertMultiple(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), keyHolder);
    }

    public <T> int insertMultiple(MultiRowInsertStatementProvider<T> insertStatement, KeyHolder keyHolder) {
        return template.update(insertStatement.getInsertStatement(),
                new BeanPropertySqlParameterSource(insertStatement), keyHolder);
    }

    public <T> List<T> selectList(Buildable<SelectModel> selectStatement, RowMapper<T> rowMapper) {
        return selectList(selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), rowMapper);
    }

    public <T> List<T> selectList(SelectStatementProvider selectStatement, RowMapper<T> rowMapper) {
        return template.query(selectStatement.getSelectStatement(), selectStatement.getParameters(), rowMapper);
    }

    public <T> Optional<T> selectOne(Buildable<SelectModel> selectStatement, RowMapper<T> rowMapper) {
        return selectOne(selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), rowMapper);
    }

    public <T> Optional<T> selectOne(SelectStatementProvider selectStatement, RowMapper<T> rowMapper) {
        T result;
        try {
            result = template.queryForObject(selectStatement.getSelectStatement(), selectStatement.getParameters(),
                            rowMapper);
        } catch (EmptyResultDataAccessException e) {
            result = null;
        }

        return Optional.ofNullable(result);
    }

    public int update(Buildable<UpdateModel> updateStatement) {
        return update(updateStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER));
    }

    public int update(UpdateStatementProvider updateStatement) {
        return template.update(updateStatement.getUpdateStatement(), updateStatement.getParameters());
    }
}
