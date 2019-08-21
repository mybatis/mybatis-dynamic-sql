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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

typealias JoinReceiver = JoinCollector.() -> JoinCollector
typealias CriteriaReceiver = CriteriaCollector.() -> CriteriaCollector

fun QueryExpressionDSL<SelectModel>.join(table: SqlTable, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            join(table, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun QueryExpressionDSL<SelectModel>.join(table: SqlTable, alias: String, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            join(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun QueryExpressionDSL<SelectModel>.fullJoin(table: SqlTable, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            fullJoin(table, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun QueryExpressionDSL<SelectModel>.fullJoin(table: SqlTable, alias: String, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            fullJoin(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun QueryExpressionDSL<SelectModel>.leftJoin(table: SqlTable, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            leftJoin(table, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun QueryExpressionDSL<SelectModel>.leftJoin(table: SqlTable, alias: String, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            leftJoin(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun QueryExpressionDSL<SelectModel>.rightJoin(table: SqlTable, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            rightJoin(table, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun QueryExpressionDSL<SelectModel>.rightJoin(table: SqlTable, alias: String, collect: JoinReceiver) =
        apply {
            val collector = JoinCollector()
            collect(collector)
            rightJoin(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
        }

fun <T> QueryExpressionDSL<SelectModel>.where(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                              collect: CriteriaReceiver) =
        apply {
            val collector = CriteriaCollector()
            collect(collector)
            where(column, condition, collector.criteria)
        }

fun <T> QueryExpressionDSL<SelectModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        apply {
            where().and(column, condition)
        }

fun <T> QueryExpressionDSL<SelectModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                            collect: CriteriaReceiver) =
        apply {
            val collector = CriteriaCollector()
            collect(collector)
            where().and(column, condition, collector.criteria)
        }

fun <T> QueryExpressionDSL<SelectModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        apply {
            where().or(column, condition)
        }

fun <T> QueryExpressionDSL<SelectModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                           collect: CriteriaReceiver) =
        apply {
            val collector = CriteriaCollector()
            collect(collector)
            where().or(column, condition, collector.criteria)
        }

fun QueryExpressionDSL<SelectModel>.allRows() = this as Buildable<SelectModel>
