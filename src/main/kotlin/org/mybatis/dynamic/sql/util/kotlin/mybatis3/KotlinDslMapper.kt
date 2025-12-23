/*
 *    Copyright 2016-2025 the original author or authors.
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

package org.mybatis.dynamic.sql.util.kotlin.mybatis3

import org.apache.ibatis.annotations.SelectProvider
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper

/**
 * @author lidiwei
 * @create 2025-12-23 14:30
 */
interface KotlinDslMapper<out Table : SqlTable, Record : Any> : CommonCountMapper, CommonDeleteMapper,
    CommonInsertMapper<Record>,
    CommonUpdateMapper {
    val table: Table

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun doSelectList(selectStatement: SelectStatementProvider): List<Record>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun doSelectOne(selectStatement: SelectStatementProvider): Record?
}

fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.insert(record: Record): Int =
    insert(
        mapper = this::insert,
        row = record,
        table = this.table
    ) {
        table.columns().forEach {  withMappedColumn(it) }
    }

fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.update(completer: UpdateCompleter): Int =
    update(this::update, table, completer)

fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.delete(completer: DeleteCompleter): Int =
    deleteFrom(this::delete, table, completer)

fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.count(completer: CountCompleter): Long =
    count(this::count, table.allColumns(), table = this.table, completer = completer)

fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.selectOne(
    selectList: List<BasicColumn> = table.columns(),
    completer: SelectCompleter
): Record? = selectOne(this::doSelectOne, selectList, table, completer)

fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.selectList(
    selectList: List<BasicColumn> = table.columns(),
    completer: SelectCompleter
): List<Record> = selectList(this::doSelectList, selectList, table, completer)


fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.insertBatch(vararg records: Record): List<Int> =
    insertBatch(records.toList())

fun <Table : SqlTable, Record : Any> KotlinDslMapper<Table, Record>.insertBatch(records: Collection<Record>): List<Int> =
    insertBatch(this::insert, records, table) {
        table.columns().forEach {  withMappedColumn(it) }
    }
