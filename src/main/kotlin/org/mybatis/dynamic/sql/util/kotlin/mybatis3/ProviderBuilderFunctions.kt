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
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter

typealias InsertCompleter<T> = InsertDSL<T>.() -> InsertDSL<T>
typealias MultiRowInsertCompleter<T> = MultiRowInsertDSL<T>.() -> MultiRowInsertDSL<T>

fun count(table: SqlTable, completer: CountCompleter) =
    completer(SqlBuilder.countFrom(table)).build().render(RenderingStrategies.MYBATIS3)

fun deleteFrom(table: SqlTable, completer: DeleteCompleter) =
    completer(SqlBuilder.deleteFrom(table)).build().render(RenderingStrategies.MYBATIS3)

fun <T> insert(record: T, table: SqlTable, completer: InsertCompleter<T>): InsertStatementProvider<T> =
    completer(SqlBuilder.insert(record).into(table)).build().render(RenderingStrategies.MYBATIS3)

fun <T> insertMultiple(records: Collection<T>, table: SqlTable, completer: MultiRowInsertCompleter<T>): MultiRowInsertStatementProvider<T> =
    completer(SqlBuilder.insertMultiple(records).into(table)).build().render(RenderingStrategies.MYBATIS3)

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable, complete: SelectCompleter) =
    complete(from(table)).build().render(RenderingStrategies.MYBATIS3)

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable, alias: String, complete: SelectCompleter) =
    complete(from(table, alias)).build().render(RenderingStrategies.MYBATIS3)

fun select(start: QueryExpressionDSL<SelectModel>, completer: SelectCompleter) =
    completer(start).build().render(RenderingStrategies.MYBATIS3)

fun select(selectList: List<BasicColumn>, table: SqlTable, completer: SelectCompleter) =
    SqlBuilder.select(selectList).from(table, completer)

fun selectDistinct(selectList: List<BasicColumn>, table: SqlTable, completer: SelectCompleter) =
    SqlBuilder.selectDistinct(selectList).from(table, completer)

fun update(table: SqlTable, completer: UpdateCompleter) =
    completer(SqlBuilder.update(table)).build().render(RenderingStrategies.MYBATIS3)
