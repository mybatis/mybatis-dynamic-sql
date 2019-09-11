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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet

fun NamedParameterJdbcTemplate.count(selectStatement: SelectStatementProvider) =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, Long::class.java)!!

fun NamedParameterJdbcTemplate.countFrom(table: SqlTable, completer: CountCompleter) =
    count(org.mybatis.dynamic.sql.util.kotlin.spring.countFrom(table, completer))

fun NamedParameterJdbcTemplate.delete(deleteStatement: DeleteStatementProvider) =
    update(deleteStatement.deleteStatement, deleteStatement.parameters)

fun NamedParameterJdbcTemplate.deleteFrom(table: SqlTable, completer: DeleteCompleter) =
    delete(org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom(table, completer))

fun <T> NamedParameterJdbcTemplate.insert(insertStatement: InsertStatementProvider<T>) =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement.record))

fun <T> NamedParameterJdbcTemplate.insert(record: T, table: SqlTable, completer: InsertCompleter<T>) =
    insert(SqlBuilder.insert(record).into(table, completer))

fun NamedParameterJdbcTemplate.select(vararg selectList: BasicColumn) =
    SelectListFromGatherer(selectList.toList(), this)

fun NamedParameterJdbcTemplate.selectDistinct(vararg selectList: BasicColumn) =
    SelectDistinctFromGatherer(selectList.toList(), this)

fun NamedParameterJdbcTemplate.selectOne(vararg selectList: BasicColumn) =
    SelectOneFromGatherer(selectList.toList(), this)

fun <T> NamedParameterJdbcTemplate.selectList(selectStatement: SelectStatementProvider, rowMapper: (rs: ResultSet, rowNum: Int) -> T): List<T> =
    query(selectStatement.selectStatement, selectStatement.parameters, rowMapper)

fun <T> NamedParameterJdbcTemplate.selectOne(selectStatement: SelectStatementProvider, rowMapper: (rs: ResultSet, rowNum: Int) -> T): T? =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, rowMapper)

fun NamedParameterJdbcTemplate.update(updateStatement: UpdateStatementProvider) =
    update(updateStatement.updateStatement, updateStatement.parameters)

fun NamedParameterJdbcTemplate.update(table: SqlTable, completer: UpdateCompleter) =
    update(org.mybatis.dynamic.sql.util.kotlin.spring.update(table, completer))

// support classes for select DSL
class SelectListFromGatherer(private val selectList: List<BasicColumn>, private val template: NamedParameterJdbcTemplate) {
    fun from(table: SqlTable, completer: SelectCompleter) =
        SelectListMapperGatherer(SqlBuilder.select(selectList).from(table, completer), template)

    fun from(table: SqlTable, alias: String, completer: SelectCompleter) =
        SelectListMapperGatherer(SqlBuilder.select(selectList).from(table, alias, completer), template)
}

class SelectDistinctFromGatherer(private val selectList: List<BasicColumn>, private val template: NamedParameterJdbcTemplate) {
    fun from(table: SqlTable, completer: SelectCompleter) =
        SelectListMapperGatherer(SqlBuilder.selectDistinct(selectList).from(table, completer), template)

    fun from(table: SqlTable, alias: String, completer: SelectCompleter) =
        SelectListMapperGatherer(SqlBuilder.selectDistinct(selectList).from(table, alias, completer), template)
}

class SelectOneFromGatherer(private val selectList: List<BasicColumn>, private val template: NamedParameterJdbcTemplate) {
    fun from(table: SqlTable, completer: SelectCompleter) =
        SelectOneMapperGatherer(SqlBuilder.select(selectList).from(table, completer), template)

    fun from(table: SqlTable, alias: String, completer: SelectCompleter) =
        SelectOneMapperGatherer(SqlBuilder.select(selectList).from(table, alias, completer), template)
}

class SelectListMapperGatherer(private val selectStatement: SelectStatementProvider, private val template: NamedParameterJdbcTemplate) {
    fun <T> withRowMapper(rowMapper: (rs: ResultSet, rowNum: Int) -> T) =
        template.selectList(selectStatement, rowMapper)
}

class SelectOneMapperGatherer(private val selectStatement: SelectStatementProvider, private val template: NamedParameterJdbcTemplate) {
    fun <T> withRowMapper(rowMapper: (rs: ResultSet, rowNum: Int) -> T) =
        template.selectOne(selectStatement, rowMapper)
}
