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
import org.mybatis.dynamic.sql.where.AbstractWhereSupportingDSL

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

typealias WhereApplier = AbstractWhereDSL<*>.() -> Unit

@MyBatisDslMarker
@Suppress("TooManyFunctions")
abstract class KotlinBaseBuilder<D: AbstractWhereSupportingDSL<*>, B: KotlinBaseBuilder<D, B>> {
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        applyToWhere {
            where(column, condition)
        }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sq ->
            where(column, condition, sq)
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
        applyToWhere(subCriteria) { sq ->
            and(column, condition, sq)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        applyToWhere {
            or(column, condition)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver) =
        applyToWhere(subCriteria) { sq ->
            or(column, condition, sq)
        }

    fun allRows() = self()

    private fun applyToWhere(block: AbstractWhereDSL<*>.() -> Unit): B {
        getDsl().where().apply(block)
        return self()
    }

    private fun applyToWhere(
        subCriteria: CriteriaReceiver,
        block: AbstractWhereDSL<*>.(List<SqlCriterion<*>>) -> Unit
    ): B {
        getDsl().where().block(CriteriaCollector().apply(subCriteria).criteria)
        return self()
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

    private fun applyToDsl(joinCriteria: JoinReceiver, applyJoin: D.(JoinCollector) -> Unit): B {
        getDsl().applyJoin(JoinCollector().apply(joinCriteria))
        return self()
    }

    private fun applyToDsl(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver,
        applyJoin: D.(KotlinQualifiedSubQueryBuilder, JoinCollector) -> Unit
    ): B {
        getDsl().applyJoin(KotlinQualifiedSubQueryBuilder().apply(subQuery), JoinCollector().apply(joinCriteria))
        return self()
    }
}
