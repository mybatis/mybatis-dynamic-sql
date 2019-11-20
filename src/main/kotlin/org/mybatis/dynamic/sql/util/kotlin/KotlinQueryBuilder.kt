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
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.*
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

typealias SelectCompleter = KotlinQueryBuilder.() -> Buildable<SelectModel>

class KotlinQueryBuilder(private val dsl: QueryExpressionDSL<SelectModel>): Buildable<SelectModel> {
    fun join(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.join(table, receiver)
            }

    fun join(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.join(table, alias, receiver)
            }

    fun fullJoin(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.fullJoin(table, receiver)
            }

    fun fullJoin(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.fullJoin(table, alias, receiver)
            }

    fun leftJoin(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.leftJoin(table, receiver)
            }

    fun leftJoin(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.leftJoin(table, alias, receiver)
            }

    fun rightJoin(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.rightJoin(table, receiver)
            }

    fun rightJoin(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.rightJoin(table, alias, receiver)
            }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>) =
            apply {
                dsl.where(column, condition)
            }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
            apply {
                dsl.where().where(column, condition, collect)
            }

    fun applyWhere(whereApplier: WhereApplier) =
            apply {
                dsl.applyWhere(whereApplier)
            }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>) =
            apply {
                dsl.where().and(column, condition)
            }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
            apply {
                dsl.where().and(column, condition, collect)
            }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
            apply {
                dsl.where().or(column, condition)
            }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
            apply {
                dsl.where().or(column, condition, collect)
            }

    fun groupBy(vararg columns: BasicColumn) =
            apply {
                dsl.groupBy(*columns)
            }

    fun orderBy(vararg columns: SortSpecification) =
            apply {
                dsl.orderBy(*columns)
            }

    fun limit(limit: Long) =
            apply {
                dsl.limit(limit)
            }

    fun offset(offset: Long) =
            apply {
                dsl.offset(offset)
            }

    fun fetchFirst(fetchFirstRows: Long) =
            apply {
                dsl.fetchFirst(fetchFirstRows)
            }

    fun allRows() = this

    override fun build(): SelectModel = dsl.build()
}
