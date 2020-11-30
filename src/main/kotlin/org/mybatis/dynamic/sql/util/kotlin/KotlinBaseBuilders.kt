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
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.AbstractQueryExpressionDSL
import org.mybatis.dynamic.sql.where.AbstractWhereDSL
import org.mybatis.dynamic.sql.where.AbstractWhereSupport
import org.mybatis.dynamic.sql.where.condition.Exists

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

typealias WhereApplier = AbstractWhereDSL<*>.() -> Unit

@MyBatisDslMarker
@Suppress("TooManyFunctions")
abstract class KotlinBaseBuilder<D: AbstractWhereSupport<*>, B: KotlinBaseBuilder<D, B>> {
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        applyToWhere {
            where(column, condition)
        }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sc ->
            where(column, condition, sc)
        }

    fun where(exists: Exists) =
        applyToWhere {
            where(exists)
        }

    fun where(exists: Exists, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sc ->
            where(exists, sc)
        }

    fun applyWhere(whereApplier: WhereApplier) =
        applyToWhere {
            applyWhere(whereApplier)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        applyToWhere {
            and(column, condition)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sc ->
            and(column, condition, sc)
        }

    fun and(exists: Exists) =
        applyToWhere {
            and(exists)
        }

    fun and(exists: Exists, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sc ->
            and(exists, sc)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        applyToWhere {
            or(column, condition)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sc ->
            or(column, condition, sc)
        }

    fun or(exists: Exists) =
        applyToWhere {
            or(exists)
        }

    fun or(exists: Exists, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sc ->
            or(exists, sc)
        }

    fun allRows() = self()

    private fun applyToWhere(block: AbstractWhereDSL<*>.() -> Unit) =
        self().also{
            getDsl().where().apply(block)
        }

    private fun applyToWhere(
        subCriteria: CriteriaReceiver,
        block: AbstractWhereDSL<*>.(List<SqlCriterion>) -> Unit
    ) =
        self().also {
            getDsl().where().block(CriteriaCollector().apply(subCriteria).criteria)
        }

    protected abstract fun self(): B

    protected abstract fun getDsl(): D
}

@Suppress("TooManyFunctions")
abstract class KotlinBaseJoiningBuilder<D: AbstractQueryExpressionDSL<*, *, *>, B: KotlinBaseJoiningBuilder<D, B>>
    : KotlinBaseBuilder<D, B>() {

    fun join(table: SqlTable, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            join(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun join(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            join(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun join(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ) =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            join(sq, sq.correlationName, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun fullJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ) =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            fullJoin(sq, sq.correlationName, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun leftJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ) =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            leftJoin(sq, sq.correlationName, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver) =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun rightJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ) =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            rightJoin(sq, sq.correlationName, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    private fun applyToDsl(joinCriteria: JoinReceiver, applyJoin: D.(JoinCollector) -> Unit) =
        self().also {
            getDsl().applyJoin(JoinCollector().apply(joinCriteria))
        }

    private fun applyToDsl(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver,
        applyJoin: D.(KotlinQualifiedSubQueryBuilder, JoinCollector) -> Unit
    ) =
        self().also {
            getDsl().applyJoin(KotlinQualifiedSubQueryBuilder().apply(subQuery), JoinCollector().apply(joinCriteria))
        }
}
