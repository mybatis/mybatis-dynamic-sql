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
package org.mybatis.dynamic.sql.util.kotlin.mybatis3

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter

fun count(mapper: (SelectStatementProvider) -> Long, table: SqlTable, completer: CountCompleter) =
    mapper(count(table, completer))

fun deleteFrom(mapper: (DeleteStatementProvider) -> Int, table: SqlTable, completer: DeleteCompleter) =
    mapper(deleteFrom(table, completer))

fun <T> insert(mapper: (InsertStatementProvider<T>) -> Int, record: T, table: SqlTable, completer: InsertCompleter<T>) =
    mapper(insert(record, table, completer))

fun <T> insertMultiple(mapper: (MultiRowInsertStatementProvider<T>) -> Int, records: Collection<T>, table: SqlTable, completer: MultiRowInsertCompleter<T>) =
    mapper(insertMultiple(records, table, completer))

fun <T> selectDistinct(mapper: (SelectStatementProvider) -> List<T>, selectList: List<BasicColumn>, table: SqlTable, completer: SelectCompleter) =
    mapper(selectDistinct(selectList, table, completer))

fun <T> selectList(mapper: (SelectStatementProvider) -> List<T>, selectList: List<BasicColumn>, table: SqlTable, completer: SelectCompleter) =
    mapper(select(selectList, table, completer))

fun <T> selectList(mapper: (SelectStatementProvider) -> List<T>, start: QueryExpressionDSL<SelectModel>, completer: SelectCompleter) =
    mapper(select(start, completer))

fun <T> selectOne(mapper: (SelectStatementProvider) -> T?, selectList: List<BasicColumn>, table: SqlTable, completer: SelectCompleter) =
    mapper(select(selectList, table, completer))

fun <T> selectOne(mapper: (SelectStatementProvider) -> T?, start: QueryExpressionDSL<SelectModel>, completer: SelectCompleter) =
    mapper(select(start, completer))

fun update(mapper: (UpdateStatementProvider) -> Int, table: SqlTable, completer: UpdateCompleter) =
    mapper(update(table, completer))
