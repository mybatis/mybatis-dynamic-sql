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
import org.mybatis.dynamic.sql.where.AbstractWhereDSL
import org.mybatis.dynamic.sql.where.AbstractWhereSupportingDSL

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

typealias WhereApplier = AbstractWhereDSL<*>.() -> Unit

@MyBatisDslMarker
@Suppress("TooManyFunctions")
abstract class KotlinBaseBuilder<D: AbstractWhereSupportingDSL<*>, B: KotlinBaseBuilder<D, B>> {
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applyToWhere {
            where(column, condition)
        }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applyToWhere {
            where(column, condition, CriteriaCollector().apply(subCriteria).criteria)
        }

    fun applyWhere(whereApplier: WhereApplier): B =
        applyToWhere {
            applyWhere(whereApplier)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applyToWhere {
            and(column, condition)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applyToWhere {
            and(column, condition, CriteriaCollector().apply(subCriteria).criteria)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        applyToWhere {
            or(column, condition)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applyToWhere {
            or(column, condition, CriteriaCollector().apply(subCriteria).criteria)
        }

    fun allRows() = self()

    private fun applyToWhere(block: AbstractWhereDSL<*>.() -> Unit): B {
        getDsl().where().apply(block)
        return self()
    }

    protected abstract fun self(): B

    protected abstract fun getDsl(): D
}

@Suppress("TooManyFunctions")
abstract class KotlinBaseJoiningBuilder<D: AbstractQueryExpressionDSL<*, *, *>, B: KotlinBaseJoiningBuilder<D, B>> : KotlinBaseBuilder<D, B>() {

    fun join(table: SqlTable, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                join(table, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun join(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                join(table, alias, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun join(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
        buildAndApply(joinCriteria) {
            val builder = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            applyToDsl {
                join(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun fullJoin(table: SqlTable, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                fullJoin(table, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun fullJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                fullJoin(table, alias, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun fullJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
        buildAndApply(joinCriteria) {
            val builder = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            applyToDsl {
                fullJoin(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun leftJoin(table: SqlTable, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                leftJoin(table, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun leftJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                leftJoin(table, alias, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun leftJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
        buildAndApply(joinCriteria) {
            val builder = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            applyToDsl {
                leftJoin(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun rightJoin(table: SqlTable, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                rightJoin(table, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun rightJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        buildAndApply(joinCriteria) {
            applyToDsl {
                rightJoin(table, alias, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    fun rightJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
        buildAndApply(joinCriteria) {
            val builder = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            applyToDsl {
                rightJoin(builder, builder.correlationName, it.onJoinCriterion, it.andJoinCriteria)
            }
        }

    private fun buildAndApply(joinCriteria: JoinReceiver, block: (JoinCollector) -> B): B {
        return block(JoinCollector().apply(joinCriteria))
    }

    private fun applyToDsl(block: D.() -> Unit): B {
        getDsl().apply(block)
        return self()
    }
}
