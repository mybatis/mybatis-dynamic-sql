/**
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
package org.mybatis.dynamic.sql.util.kotlin.mybatis3

import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.CountDSL
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*

fun countFrom(table: SqlTable, completer: CountCompleter): SelectStatementProvider {
    val builder = KotlinCountBuilder(SqlBuilder.countFrom(table))
    completer(builder)
    return builder.build().render(RenderingStrategies.MYBATIS3)
}

fun deleteFrom(table: SqlTable, completer: DeleteCompleter): DeleteStatementProvider {
    val builder = KotlinDeleteBuilder(SqlBuilder.deleteFrom(table))
    completer(builder)
    return builder.build().render(RenderingStrategies.MYBATIS3)
}

fun <T> InsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: InsertCompleter<T>): InsertStatementProvider<T> =
    completer(into(table)).build().render(RenderingStrategies.MYBATIS3)

fun <T> MultiRowInsertDSL.IntoGatherer<T>.into(
    table: SqlTable,
    completer: MultiRowInsertCompleter<T>
): MultiRowInsertStatementProvider<T> =
    completer(into(table)).build().render(RenderingStrategies.MYBATIS3)

fun CountDSL.FromGatherer<SelectModel>.from(
    table: SqlTable,
    completer: CountCompleter
): SelectStatementProvider {
    val builder = KotlinCountBuilder(from(table))
    completer(builder)
    return builder.build().render(RenderingStrategies.MYBATIS3)
}

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(
    table: SqlTable,
    completer: SelectCompleter
): SelectStatementProvider {
    val builder = KotlinQueryBuilder(from(table))
    completer(builder)
    return builder.build().render(RenderingStrategies.MYBATIS3)
}

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(
    table: SqlTable,
    alias: String,
    completer: SelectCompleter
): SelectStatementProvider {
    val builder = KotlinQueryBuilder(from(table, alias))
    completer(builder)
    return builder.build().render(RenderingStrategies.MYBATIS3)
}

fun select(start: QueryExpressionDSL<SelectModel>, completer: SelectCompleter): SelectStatementProvider {
    val builder = KotlinQueryBuilder(start)
    completer(builder)
    return builder.build().render(RenderingStrategies.MYBATIS3)
}

fun update(table: SqlTable, completer: UpdateCompleter): UpdateStatementProvider {
    val builder = KotlinUpdateBuilder(SqlBuilder.update(table))
    completer(builder)
    return builder.build().render(RenderingStrategies.MYBATIS3)
}

fun insertInto(table: SqlTable, completer: GeneralInsertCompleter): GeneralInsertStatementProvider =
    completer(GeneralInsertDSL.insertInto(table)).build().render(RenderingStrategies.MYBATIS3)
