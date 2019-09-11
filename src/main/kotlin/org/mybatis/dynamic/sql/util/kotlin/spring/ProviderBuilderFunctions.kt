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

import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.kotlin.*

fun countFrom(table: SqlTable, completer: CountCompleter) =
    completer(SqlBuilder.countFrom(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun deleteFrom(table: SqlTable, completer: DeleteCompleter) =
    completer(SqlBuilder.deleteFrom(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun <T> InsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: InsertCompleter<T>): InsertStatementProvider<T> =
    completer(into(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable, completer: SelectCompleter) =
    completer(from(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable, alias: String, completer: SelectCompleter) =
    completer(from(table, alias)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)

fun update(table: SqlTable, completer: UpdateCompleter) =
    completer(SqlBuilder.update(table)).build().render(RenderingStrategies.SPRING_NAMED_PARAMETER)
