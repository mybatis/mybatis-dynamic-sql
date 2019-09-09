/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.util.kotlin.spring

import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet

fun NamedParameterJdbcTemplate.count(selectStatement: SelectStatementProvider) =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, Long::class.java)!!

fun NamedParameterJdbcTemplate.delete(deleteStatement: DeleteStatementProvider) =
    update(deleteStatement.deleteStatement, deleteStatement.parameters)

fun <T> NamedParameterJdbcTemplate.insert(insertStatement: InsertStatementProvider<T>) =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement.record))

fun <T> NamedParameterJdbcTemplate.selectMany(selectStatement: SelectStatementProvider, rowMapper: (rs: ResultSet, rowNum: Int) -> T): List<T> =
    query(selectStatement.selectStatement, selectStatement.parameters, rowMapper)

fun <T> NamedParameterJdbcTemplate.selectOne(selectStatement: SelectStatementProvider, rowMapper: (rs: ResultSet, rowNum: Int) -> T): T? =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, rowMapper)

fun NamedParameterJdbcTemplate.update(updateStatement: UpdateStatementProvider) =
    update(updateStatement.updateStatement, updateStatement.parameters)
