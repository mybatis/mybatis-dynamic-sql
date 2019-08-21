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

import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.DeleteDSL
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils

fun deleteFrom(table: SqlTable, complete: DeleteDSL<DeleteModel>.() -> Buildable<DeleteModel>) =
        complete(SqlBuilder.deleteFrom(table)).build().render(RenderingStrategies.MYBATIS3)

fun <T> insert(mapper: (InsertStatementProvider<T>) -> Int, record: T, table: SqlTable,
               completer: InsertDSL<T>.() -> InsertDSL<T>) =
        MyBatis3Utils.insert(mapper, record, table, completer)

fun <T> insertMultiple(mapper: (MultiRowInsertStatementProvider<T>) -> Int, records: Collection<T>, table: SqlTable,
                       completer: MultiRowInsertDSL<T>.() -> MultiRowInsertDSL<T>) =
        MyBatis3Utils.insertMultiple(mapper, records, table, completer)

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable,
                                                      complete: QueryExpressionDSL<SelectModel>.() -> Buildable<SelectModel>) =
        complete(from(table)).build().render(RenderingStrategies.MYBATIS3)

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable, alias: String,
                                                      complete: QueryExpressionDSL<SelectModel>.() -> Buildable<SelectModel>) =
        complete(from(table, alias)).build().render(RenderingStrategies.MYBATIS3)

fun update(table: SqlTable, complete: UpdateDSL<UpdateModel>.() -> Buildable<UpdateModel>) =
    complete(SqlBuilder.update(table)).build().render(RenderingStrategies.MYBATIS3)
