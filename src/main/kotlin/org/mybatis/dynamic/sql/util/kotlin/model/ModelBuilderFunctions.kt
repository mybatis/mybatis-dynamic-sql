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
package org.mybatis.dynamic.sql.util.kotlin.model

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.insert.BatchInsertDSL
import org.mybatis.dynamic.sql.insert.BatchInsertModel
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.InsertModel
import org.mybatis.dynamic.sql.insert.InsertSelectModel
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.kotlin.BatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinCountBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinDeleteBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinInsertSelectSubQueryBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinSelectBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinUpdateBuilder
import org.mybatis.dynamic.sql.util.kotlin.MultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter

/**
 * Collection of functions that will create various DSL models in a Kotlin native way.
 */

fun count(column: BasicColumn, completer: CountCompleter): SelectModel =
    KotlinCountBuilder(SqlBuilder.countColumn(column)).apply(completer).build()

fun countDistinct(column: BasicColumn, completer: CountCompleter): SelectModel =
    KotlinCountBuilder(SqlBuilder.countDistinctColumn(column)).apply(completer).build()

fun countFrom(table: SqlTable, completer: CountCompleter): SelectModel =
    KotlinCountBuilder(SqlBuilder.countColumn(SqlBuilder.constant<Long>("*")))
        .from(table).apply(completer).build()

fun deleteFrom(table: SqlTable, completer: DeleteCompleter): DeleteModel =
    KotlinDeleteBuilder(SqlBuilder.deleteFrom(table)).apply(completer).build()

fun insertInto(table: SqlTable, completer: GeneralInsertCompleter): GeneralInsertModel =
    GeneralInsertDSL.insertInto(table).apply(completer).build()

fun insertSelect(table: SqlTable, completer: InsertSelectCompleter): InsertSelectModel =
    with(KotlinInsertSelectSubQueryBuilder().apply(completer)) {
        SqlBuilder.insertInto(table)
            .withColumnList(columnList)
            .withSelectStatement(this)
            .build()
    }

fun <T> BatchInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: BatchInsertCompleter<T>): BatchInsertModel<T> =
    into(table).apply(completer).build()

fun <T> InsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: InsertCompleter<T>): InsertModel<T> =
    into(table).apply(completer).build()

fun <T> MultiRowInsertDSL.IntoGatherer<T>.into(
    table: SqlTable,
    completer: MultiRowInsertCompleter<T>
): MultiRowInsertModel<T> =
    into(table).apply(completer).build()

fun select(vararg columns: BasicColumn, completer: SelectCompleter): SelectModel =
    select(columns.asList(), completer)

fun select(columns: List<BasicColumn>, completer: SelectCompleter): SelectModel =
    KotlinSelectBuilder(SqlBuilder.select(columns)).apply(completer).build()

fun selectDistinct(vararg columns: BasicColumn, completer: SelectCompleter): SelectModel =
    selectDistinct(columns.asList(), completer)

fun selectDistinct(columns: List<BasicColumn>, completer: SelectCompleter): SelectModel =
    KotlinSelectBuilder(SqlBuilder.selectDistinct(columns)).apply(completer).build()

fun update(table: SqlTable, completer: UpdateCompleter): UpdateModel =
    KotlinUpdateBuilder(SqlBuilder.update(table)).apply(completer).build()
