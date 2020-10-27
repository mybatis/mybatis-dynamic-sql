/*
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
package org.mybatis.dynamic.sql.util.kotlin.spring

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.BatchInsertDSL
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.CountDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.kotlin.BatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinCountBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinDeleteBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinSelectBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinUpdateBuilder
import org.mybatis.dynamic.sql.util.kotlin.MultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter

fun countFrom(table: SqlTable, completer: CountCompleter) =
    completer(KotlinCountBuilder(SqlBuilder.countFrom(table))).build()
        .render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun deleteFrom(table: SqlTable, completer: DeleteCompleter) =
    completer(KotlinDeleteBuilder(SqlBuilder.deleteFrom(table))).build()
        .render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun insertInto(table: SqlTable, completer: GeneralInsertCompleter) =
    completer(GeneralInsertDSL.insertInto(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun <T> BatchInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: BatchInsertCompleter<T>) =
    completer(into(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun <T> InsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: InsertCompleter<T>) =
    completer(into(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun <T> MultiRowInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: MultiRowInsertCompleter<T>) =
    completer(into(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun CountDSL.FromGatherer<SelectModel>.from(table: SqlTable, completer: CountCompleter) =
    completer(KotlinCountBuilder(from(table))).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun select(
    vararg columnList: BasicColumn,
    complete: SelectCompleter
) =
    complete(org.mybatis.dynamic.sql.util.kotlin.select(columnList.asList())).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun select(
    columnList: List<BasicColumn>,
    complete: SelectCompleter
) =
    complete(org.mybatis.dynamic.sql.util.kotlin.select(columnList)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun select(
    selectBuilder: KotlinSelectBuilder,
    complete: SelectCompleter
) =
    complete(selectBuilder).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun selectDistinct(
    selectBuilder: KotlinSelectBuilder,
    complete: SelectCompleter
) =
    complete(selectBuilder).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun update(table: SqlTable, completer: UpdateCompleter) =
    completer(KotlinUpdateBuilder(SqlBuilder.update(table))).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

