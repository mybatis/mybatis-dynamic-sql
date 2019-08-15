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
package org.mybatis.dynamic.sql.util.mybatis3.kotlin

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.DeleteDSL
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.CompletableQuery
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.Buildable

typealias CountHelper = CompletableQuery<SelectModelAdapter<Long>>.() -> Buildable<SelectModelAdapter<Long>>
typealias SelectListHelper<T> = CompletableQuery<SelectModelAdapter<List<T>>>.() -> Buildable<SelectModelAdapter<List<T>>>
typealias SelectOneHelper<T> = CompletableQuery<SelectModelAdapter<T?>>.() -> Buildable<SelectModelAdapter<T?>>

class SelectModelAdapter<R>(private val selectModel: SelectModel, private val mapperMethod: (SelectStatementProvider) -> R) {
    fun execute() = mapperMethod(selectModel.render(RenderingStrategy.MYBATIS3))
}

fun <R> selectWithKotlinMapper(mapperMethod: (SelectStatementProvider) -> R, vararg selectList: BasicColumn) =
        SelectDSL.select({ SelectModelAdapter(it, mapperMethod) }, selectList) as QueryExpressionDSL.FromGatherer<SelectModelAdapter<R>>

fun <R> selectDistinctWithKotlinMapper(mapperMethod: (SelectStatementProvider) -> R, vararg selectList: BasicColumn) =
        SelectDSL.selectDistinct({ SelectModelAdapter(it, mapperMethod) }, selectList) as QueryExpressionDSL.FromGatherer<SelectModelAdapter<R>>

fun deleteWithKotlinMapper(mapperMethod: (DeleteStatementProvider) -> Int, table: SqlTable,
                           complete: DeleteDSL<DeleteModel>.() -> Buildable<DeleteModel>) =
        mapperMethod(deleteFrom(table, complete))

fun updateWithKotlinMapper(mapperMethod: (UpdateStatementProvider) -> Int, table: SqlTable,
                           complete: UpdateDSL<UpdateModel>.() -> Buildable<UpdateModel>) =
        mapperMethod(update(table, complete))

fun <T> CompletableQuery<SelectModelAdapter<T>>.allRows() = this as Buildable<SelectModelAdapter<T>>
fun <T> CompletableQuery<SelectModelAdapter<T>>.allRowsOrderedBy(vararg columns: SortSpecification) =
        orderBy(*columns) as Buildable<SelectModelAdapter<T>>

/**
 * Functions for use with raw MyBatis3 Mappers
 */
fun deleteFrom(table: SqlTable, complete: DeleteDSL<DeleteModel>.() -> Buildable<DeleteModel>): DeleteStatementProvider {
    val dsl = SqlBuilder.deleteFrom(table)
    complete(dsl)
    return dsl.build().render(RenderingStrategy.MYBATIS3)
}

fun update(table: SqlTable, complete: UpdateDSL<UpdateModel>.() -> Buildable<UpdateModel>): UpdateStatementProvider {
    val dsl = SqlBuilder.update(table)
    complete(dsl)
    return dsl.build().render(RenderingStrategy.MYBATIS3)
}
