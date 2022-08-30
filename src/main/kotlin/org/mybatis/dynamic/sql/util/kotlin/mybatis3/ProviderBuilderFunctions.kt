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
@file:Suppress("TooManyFunctions")
package org.mybatis.dynamic.sql.util.kotlin.mybatis3

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.BatchInsertDSL
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.render.BatchInsert
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinBatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinMultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.model.count
import org.mybatis.dynamic.sql.util.kotlin.model.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.model.countFrom
import org.mybatis.dynamic.sql.util.kotlin.model.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.model.insert
import org.mybatis.dynamic.sql.util.kotlin.model.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.model.insertInto
import org.mybatis.dynamic.sql.util.kotlin.model.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.model.insertSelect
import org.mybatis.dynamic.sql.util.kotlin.model.into
import org.mybatis.dynamic.sql.util.kotlin.model.select
import org.mybatis.dynamic.sql.util.kotlin.model.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.model.update

fun count(column: BasicColumn, completer: CountCompleter): SelectStatementProvider =
    count(column, completer).render(RenderingStrategies.MYBATIS3)

fun countDistinct(column: BasicColumn, completer: CountCompleter): SelectStatementProvider =
    countDistinct(column, completer).render(RenderingStrategies.MYBATIS3)

fun countFrom(table: SqlTable, completer: CountCompleter): SelectStatementProvider =
    countFrom(table, completer).render(RenderingStrategies.MYBATIS3)

fun deleteFrom(table: SqlTable, completer: DeleteCompleter): DeleteStatementProvider =
    deleteFrom(table, completer).render(RenderingStrategies.MYBATIS3)

fun deleteFrom(table: SqlTable, tableAlias: String, completer: DeleteCompleter): DeleteStatementProvider =
    deleteFrom(table, tableAlias, completer).render(RenderingStrategies.MYBATIS3)

fun <T : Any> insert(row: T, completer: KotlinInsertCompleter<T>): InsertStatementProvider<T> =
    insert(row, completer).render(RenderingStrategies.MYBATIS3)

fun <T : Any> insertBatch(rows: Collection<T>, completer: KotlinBatchInsertCompleter<T>): BatchInsert<T> =
    insertBatch(rows, completer).render(RenderingStrategies.MYBATIS3)

fun insertInto(table: SqlTable, completer: GeneralInsertCompleter): GeneralInsertStatementProvider =
    insertInto(table, completer).render(RenderingStrategies.MYBATIS3)

fun <T : Any> insertMultiple(
    rows: Collection<T>,
    completer: KotlinMultiRowInsertCompleter<T>
): MultiRowInsertStatementProvider<T> =
    insertMultiple(rows, completer).render(RenderingStrategies.MYBATIS3)

@Deprecated("Please use the new form - move the table into the lambda with into(table)")
fun insertSelect(table: SqlTable, completer: InsertSelectCompleter): InsertSelectStatementProvider =
    insertSelect(table, completer).render(RenderingStrategies.MYBATIS3)

fun insertSelect(completer: InsertSelectCompleter): InsertSelectStatementProvider =
    insertSelect(completer).render(RenderingStrategies.MYBATIS3)

@Deprecated("Please switch to the insertBatch statement in the mybatis3 package")
fun <T> BatchInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: BatchInsertDSL<T>.() -> Unit): BatchInsert<T> =
    into(table, completer).render(RenderingStrategies.MYBATIS3)

@Deprecated("Please switch to the insert statement in the mybatis3 package")
fun <T> InsertDSL.IntoGatherer<T>.into(
    table: SqlTable,
    completer: InsertDSL<T>.() -> Unit
): InsertStatementProvider<T> =
    into(table, completer).render(RenderingStrategies.MYBATIS3)

@Deprecated("Please switch to the insertMultiple statement in the mybatis3 package")
fun <T> MultiRowInsertDSL.IntoGatherer<T>.into(
    table: SqlTable,
    completer: MultiRowInsertDSL<T>.() -> Unit
): MultiRowInsertStatementProvider<T> =
    into(table, completer).render(RenderingStrategies.MYBATIS3)

fun select(vararg columns: BasicColumn, completer: SelectCompleter): SelectStatementProvider =
    select(columns.asList(), completer).render(RenderingStrategies.MYBATIS3)

fun select(columns: List<BasicColumn>, completer: SelectCompleter): SelectStatementProvider =
    select(columns, completer).render(RenderingStrategies.MYBATIS3)

fun selectDistinct(vararg columns: BasicColumn, completer: SelectCompleter): SelectStatementProvider =
    selectDistinct(columns.asList(), completer).render(RenderingStrategies.MYBATIS3)

fun selectDistinct(columns: List<BasicColumn>, completer: SelectCompleter): SelectStatementProvider =
    selectDistinct(columns, completer).render(RenderingStrategies.MYBATIS3)

fun update(table: SqlTable, completer: UpdateCompleter): UpdateStatementProvider =
    update(table, completer).render(RenderingStrategies.MYBATIS3)

fun update(table: SqlTable, tableAlias: String, completer: UpdateCompleter): UpdateStatementProvider =
    update(table, tableAlias, completer).render(RenderingStrategies.MYBATIS3)
