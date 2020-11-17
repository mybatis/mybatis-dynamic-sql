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
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.AbstractQueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.where.AbstractWhereDSL

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

typealias WhereApplier = AbstractWhereDSL<*>.() -> Unit

@MyBatisDslMarker
@Suppress("TooManyFunctions")
abstract class KotlinBaseBuilder<W : AbstractWhereDSL<W>, B : KotlinBaseBuilder<W, B>> {
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applySelf {
            getWhere().where(column, condition)
        }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applySelf {
            getWhere().where(column, condition, subCriteria(CriteriaCollector()).criteria)
        }

    fun applyWhere(whereApplier: WhereApplier): B =
        applySelf {
            getWhere().applyWhere(whereApplier)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applySelf {
            getWhere().and(column, condition)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applySelf {
            getWhere().and(column, condition, subCriteria(CriteriaCollector()).criteria)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applySelf {
            getWhere().or(column, condition)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applySelf {
            getWhere().or(column, condition, subCriteria(CriteriaCollector()).criteria)
        }

    fun allRows() = self()

    protected fun applySelf(block: B.() -> Unit): B =
        self().apply { block() }

    protected abstract fun self(): B

    protected abstract fun getWhere(): W
}

@Suppress("TooManyFunctions")
abstract class KotlinBaseJoiningBuilder<T : AbstractQueryExpressionDSL<T, SelectModel>, W : AbstractWhereDSL<W>,
        B : KotlinBaseJoiningBuilder<T, W, B>> : KotlinBaseBuilder<W, B>() {

    fun join(table: SqlTable, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().join(table, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun join(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().join(table, alias, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun join(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> KotlinQualifiedSubQueryBuilder,
        joinCriteria: JoinReceiver
    ) =
        applyJoin(joinCriteria) {
            val builder = subQuery(KotlinQualifiedSubQueryBuilder())
            getDsl().join(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().fullJoin(table, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().fullJoin(table, alias, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun fullJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> KotlinQualifiedSubQueryBuilder,
        joinCriteria: JoinReceiver
    ) =
        applyJoin(joinCriteria) {
            val builder = subQuery(KotlinQualifiedSubQueryBuilder())
            getDsl().fullJoin(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().leftJoin(table, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().leftJoin(table, alias, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun leftJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> KotlinQualifiedSubQueryBuilder,
        joinCriteria: JoinReceiver
    ) =
        applyJoin(joinCriteria) {
            val builder = subQuery(KotlinQualifiedSubQueryBuilder())
            getDsl().leftJoin(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().rightJoin(table, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyJoin(joinCriteria) {
            getDsl().rightJoin(table, alias, it.onJoinCriterion, it.andJoinCriteria)
        }

    fun rightJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> KotlinQualifiedSubQueryBuilder,
        joinCriteria: JoinReceiver
    ) =
        applyJoin(joinCriteria) {
            val builder = subQuery(KotlinQualifiedSubQueryBuilder())
            getDsl().rightJoin(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
        }

    private fun applyJoin(joinCriteria: JoinReceiver, block: (JoinCollector) -> Unit) =
        applySelf {
            joinCriteria(JoinCollector()).also(block)
        }

    protected abstract fun getDsl(): AbstractQueryExpressionDSL<T, SelectModel>
}
