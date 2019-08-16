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
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.CompletableQuery
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.join.JoinCondition
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.select.whereBuilder
import org.mybatis.dynamic.sql.util.Buildable

fun QueryExpressionDSL<SelectModel>.JoinSpecificationStarter.on(joinColumn: BasicColumn, joinCondition: JoinCondition,
                                                                builderAction: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>): SelectStatementProvider {
    val fred: CompletableQuery<SelectModel> = this.on(joinColumn, joinCondition)
    builderAction(fred)
    return fred.build().render(RenderingStrategy.MYBATIS3)
}

fun QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher.and(joinColumn: BasicColumn, joinCondition: JoinCondition,
                                                                  builderAction: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>): SelectStatementProvider {
    val fred: CompletableQuery<SelectModel> = this.and(joinColumn, joinCondition)
    builderAction(fred)
    return fred.build().render(RenderingStrategy.MYBATIS3)
}

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable,
                                                      builderAction: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>): SelectStatementProvider {
    val fred: CompletableQuery<SelectModel> = this.from(table)
    builderAction(fred)
    return fred.build().render(RenderingStrategy.MYBATIS3)
}

fun <T> QueryExpressionDSL<SelectModel>.where(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                              collect: CriteriaCollector.() -> CriteriaCollector): QueryExpressionDSL<SelectModel> {
    val collector = CriteriaCollector()
    collect(collector)
    this.where(column, condition, *collector.criteria())
    return this
}

fun <T> QueryExpressionDSL<SelectModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>): QueryExpressionDSL<SelectModel> {
    whereBuilder()?.and(column, condition)
    return this
}

fun <T> QueryExpressionDSL<SelectModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                            collect: CriteriaCollector.() -> CriteriaCollector): QueryExpressionDSL<SelectModel> {
    val collector = CriteriaCollector()
    collect(collector)
    whereBuilder()?.and(column, condition, *collector.criteria())
    return this
}

fun <T> QueryExpressionDSL<SelectModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>): QueryExpressionDSL<SelectModel> {
    whereBuilder()?.or(column, condition)
    return this
}

fun <T> QueryExpressionDSL<SelectModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                           collect: CriteriaCollector.() -> CriteriaCollector): QueryExpressionDSL<SelectModel> {
    val collector = CriteriaCollector()
    collect(collector)
    whereBuilder()?.or(column, condition, *collector.criteria())
    return this
}
