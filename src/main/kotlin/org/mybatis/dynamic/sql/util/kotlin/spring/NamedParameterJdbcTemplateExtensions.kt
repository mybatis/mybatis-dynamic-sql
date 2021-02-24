/*
 *    Copyright 2016-2021 the original author or authors.
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
@file:Suppress("TooManyFunctions")

package org.mybatis.dynamic.sql.util.kotlin.spring

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.BatchInsert
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.BatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.MultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.MyBatisDslMarker
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.jdbc.support.KeyHolder
import java.sql.ResultSet

fun NamedParameterJdbcTemplate.count(selectStatement: SelectStatementProvider) =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, Long::class.java)!!

fun NamedParameterJdbcTemplate.count(column: BasicColumn, completer: CountCompleter) =
    count(org.mybatis.dynamic.sql.util.kotlin.spring.count(column, completer))

fun NamedParameterJdbcTemplate.countDistinct(column: BasicColumn, completer: CountCompleter) =
    count(org.mybatis.dynamic.sql.util.kotlin.spring.countDistinct(column, completer))

fun NamedParameterJdbcTemplate.countFrom(table: SqlTable, completer: CountCompleter) =
    count(org.mybatis.dynamic.sql.util.kotlin.spring.countFrom(table, completer))

fun NamedParameterJdbcTemplate.delete(deleteStatement: DeleteStatementProvider) =
    update(deleteStatement.deleteStatement, deleteStatement.parameters)

fun NamedParameterJdbcTemplate.deleteFrom(table: SqlTable, completer: DeleteCompleter) =
    delete(org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom(table, completer))

// batch insert
fun <T> NamedParameterJdbcTemplate.insertBatch(insertStatement: BatchInsert<T>): IntArray =
    batchUpdate(insertStatement.insertStatementSQL, SqlParameterSourceUtils.createBatch(insertStatement.records))

fun <T> NamedParameterJdbcTemplate.insertBatch(vararg records: T) =
    insertBatch(records.asList())

fun <T> NamedParameterJdbcTemplate.insertBatch(records: List<T>) =
    BatchInsertHelper(records, this)

// single record insert
fun <T> NamedParameterJdbcTemplate.insert(insertStatement: InsertStatementProvider<T>) =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement.record))

fun <T> NamedParameterJdbcTemplate.insert(insertStatement: InsertStatementProvider<T>, keyHolder: KeyHolder) =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement.record), keyHolder)

fun <T> NamedParameterJdbcTemplate.insert(record: T) =
    SingleRowInsertHelper(record, this)

// general insert
fun NamedParameterJdbcTemplate.generalInsert(insertStatement: GeneralInsertStatementProvider) =
    update(insertStatement.insertStatement, insertStatement.parameters)

fun NamedParameterJdbcTemplate.generalInsert(insertStatement: GeneralInsertStatementProvider, keyHolder: KeyHolder) =
    update(insertStatement.insertStatement, MapSqlParameterSource(insertStatement.parameters), keyHolder)

fun NamedParameterJdbcTemplate.insertInto(table: SqlTable, completer: GeneralInsertCompleter) =
    generalInsert(org.mybatis.dynamic.sql.util.kotlin.spring.insertInto(table, completer))

// multiple record insert
fun <T> NamedParameterJdbcTemplate.insertMultiple(vararg records: T) =
    insertMultiple(records.asList())

fun <T> NamedParameterJdbcTemplate.insertMultiple(records: List<T>) =
    MultiRowInsertHelper(records, this)

fun <T> NamedParameterJdbcTemplate.insertMultiple(insertStatement: MultiRowInsertStatementProvider<T>) =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement))

fun <T> NamedParameterJdbcTemplate.insertMultiple(
    insertStatement: MultiRowInsertStatementProvider<T>,
    keyHolder: KeyHolder
) =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement), keyHolder)

fun NamedParameterJdbcTemplate.insertSelect(table: SqlTable, completer: InsertSelectCompleter) =
    insertSelect(org.mybatis.dynamic.sql.util.kotlin.spring.insertSelect(table, completer))

fun NamedParameterJdbcTemplate.insertSelect(insertStatement: InsertSelectStatementProvider) =
    update(insertStatement.insertStatement, MapSqlParameterSource(insertStatement.parameters))

// insert with KeyHolder support
fun NamedParameterJdbcTemplate.withKeyHolder(keyHolder: KeyHolder, build: KeyHolderHelper.() -> Int) =
    build(KeyHolderHelper(keyHolder, this))

fun NamedParameterJdbcTemplate.select(vararg selectList: BasicColumn, completer: SelectCompleter) =
    select(selectList.toList(), completer)

fun NamedParameterJdbcTemplate.select(selectList: List<BasicColumn>, completer: SelectCompleter) =
    SelectListMapperGatherer(org.mybatis.dynamic.sql.util.kotlin.spring.select(selectList, completer), this)

fun NamedParameterJdbcTemplate.selectDistinct(vararg selectList: BasicColumn, completer: SelectCompleter) =
    selectDistinct(selectList.toList(), completer)

fun NamedParameterJdbcTemplate.selectDistinct(selectList: List<BasicColumn>, completer: SelectCompleter) =
    SelectListMapperGatherer(
        org.mybatis.dynamic.sql.util.kotlin.spring.selectDistinct(selectList, completer),
        this
    )

fun NamedParameterJdbcTemplate.selectOne(vararg selectList: BasicColumn, completer: SelectCompleter) =
    selectOne(selectList.toList(), completer)

fun NamedParameterJdbcTemplate.selectOne(selectList: List<BasicColumn>, completer: SelectCompleter) =
    SelectOneMapperGatherer(
        org.mybatis.dynamic.sql.util.kotlin.spring.select(selectList, completer),
        this
    )

fun <T> NamedParameterJdbcTemplate.selectList(
    selectStatement: SelectStatementProvider,
    rowMapper: (rs: ResultSet, rowNum: Int) -> T
): List<T> =
    query(selectStatement.selectStatement, selectStatement.parameters, rowMapper)

fun <T> NamedParameterJdbcTemplate.selectOne(
    selectStatement: SelectStatementProvider,
    rowMapper: (rs: ResultSet, rowNum: Int) -> T
): T? = try {
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, rowMapper)
} catch (e: EmptyResultDataAccessException) {
    null
}

fun NamedParameterJdbcTemplate.update(updateStatement: UpdateStatementProvider) =
    update(updateStatement.updateStatement, updateStatement.parameters)

fun NamedParameterJdbcTemplate.update(table: SqlTable, completer: UpdateCompleter) =
    update(org.mybatis.dynamic.sql.util.kotlin.spring.update(table, completer))

// support classes for select DSL
@MyBatisDslMarker
class SelectListMapperGatherer(
    private val selectStatement: SelectStatementProvider,
    private val template: NamedParameterJdbcTemplate
) {
    fun <T> withRowMapper(rowMapper: (rs: ResultSet, rowNum: Int) -> T) =
        template.selectList(selectStatement, rowMapper)
}

@MyBatisDslMarker
class SelectOneMapperGatherer(
    private val selectStatement: SelectStatementProvider,
    private val template: NamedParameterJdbcTemplate
) {
    fun <T> withRowMapper(rowMapper: (rs: ResultSet, rowNum: Int) -> T) =
        template.selectOne(selectStatement, rowMapper)
}

@MyBatisDslMarker
class KeyHolderHelper(private val keyHolder: KeyHolder, private val template: NamedParameterJdbcTemplate) {
    fun insertInto(table: SqlTable, completer: GeneralInsertCompleter) =
        template.generalInsert(org.mybatis.dynamic.sql.util.kotlin.spring.insertInto(table, completer), keyHolder)

    fun <T> insert(record: T) =
        SingleRowInsertHelper(record, template, keyHolder)

    fun <T> insertMultiple(vararg records: T) =
        insertMultiple(records.asList())

    fun <T> insertMultiple(records: List<T>) =
        MultiRowInsertHelper(records, template, keyHolder)
}

@MyBatisDslMarker
class BatchInsertHelper<T>(private val records: List<T>, private val template: NamedParameterJdbcTemplate) {
    fun into(table: SqlTable, completer: BatchInsertCompleter<T>) =
        template.insertBatch(SqlBuilder.insertBatch(records).into(table, completer))
}

@MyBatisDslMarker
class MultiRowInsertHelper<T>(
    private val records: List<T>,
    private val template: NamedParameterJdbcTemplate,
    private val keyHolder: KeyHolder? = null
) {
    fun into(table: SqlTable, completer: MultiRowInsertCompleter<T>) =
        with(SqlBuilder.insertMultiple(records).into(table, completer)) {
            keyHolder?.let { template.insertMultiple(this, it) } ?: template.insertMultiple(this)
        }
}

@MyBatisDslMarker
class SingleRowInsertHelper<T>(
    private val record: T,
    private val template: NamedParameterJdbcTemplate,
    private val keyHolder: KeyHolder? = null
) {
    fun into(table: SqlTable, completer: InsertCompleter<T>) =
        with(SqlBuilder.insert(record).into(table, completer)) {
            keyHolder?.let { template.insert(this, it) } ?: template.insert(this)
        }
}
