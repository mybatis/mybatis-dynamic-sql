/*
 *    Copyright 2016-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.select.AbstractQueryExpressionDSL
import org.mybatis.dynamic.sql.where.AbstractWhereStarter

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

@Deprecated("Please use GroupingCriteriaCollector.where")
typealias WhereApplier = KotlinBaseBuilder<*>.() -> Unit

@Deprecated("Please use GroupingCriteriaCollector.where")
fun WhereApplier.andThen(after: WhereApplier): WhereApplier = {
    invoke(this)
    after(this)
}

@MyBatisDslMarker
@Suppress("TooManyFunctions")
abstract class KotlinBaseBuilder<D : AbstractWhereStarter<*,*>> {

    fun configureStatement(c: StatementConfiguration.() -> Unit) {
        getDsl().configureStatement(c)
    }

    fun where(criteria: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteria).let {
            getDsl().where(it.initialCriterion, it.subCriteria)
        }

    fun where(criteria: List<AndOrCriteriaGroup>) {
        getDsl().where(criteria)
    }

    @Deprecated("Please move the \"and\" function into the where lambda. If the where lambda has more than one condition, you may need to surround the existing conditions with \"group\" first.")
    fun and(criteria: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteria).let {
            getDsl().where().and(it.initialCriterion, it.subCriteria)
        }

    @Deprecated("Please move the \"and\" function into the where lambda. If the where lambda has more than one condition, you may need to surround the existing conditions with \"group\" first.")
    fun and(criteria: List<AndOrCriteriaGroup>) {
        getDsl().where().and(criteria)
    }

    @Deprecated("Please move the \"or\" function into the where lambda. If the where lambda has more than one condition, you may need to surround the existing conditions with \"group\" first.")
    fun or(criteria: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteria).let {
            getDsl().where().or(it.initialCriterion, it.subCriteria)
        }

    @Deprecated("Please move the \"or\" function into the where lambda. If the where lambda has more than one condition, you may need to surround the existing conditions with \"group\" first.")
    fun or(criteria: List<AndOrCriteriaGroup>) {
        getDsl().where().or(criteria)
    }

    @Deprecated("Please use GroupingCriteriaCollector.where, then pass it to the \"where\" method")
    fun applyWhere(whereApplier: WhereApplier) = whereApplier.invoke(this)

    /**
     * This function does nothing, but it can be used to make some code snippets more understandable.
     *
     * For example, to count all rows in a table you can write either of the following:
     *
     * val rows = countFrom(foo) { }
     *
     *    or
     *
     * val rows = countFrom(foo) { allRows() }
     */
    @SuppressWarnings("EmptyFunctionBlock")
    fun allRows() {
        // intentionally empty - this function exists for code beautification and clarity only
    }

    protected abstract fun getDsl(): D
}

@Suppress("TooManyFunctions")
abstract class KotlinBaseJoiningBuilder<D : AbstractQueryExpressionDSL<*, *>> : KotlinBaseBuilder<D>() {

    fun join(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            join(table, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun join(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            join(table, alias, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun join(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            join(sq, sq.correlationName, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, alias, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun fullJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            fullJoin(sq, sq.correlationName, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, alias, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun leftJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            leftJoin(sq, sq.correlationName, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, alias, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    fun rightJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            rightJoin(sq, sq.correlationName, jc.onJoinCriterion(), jc.andJoinCriteria)
        }

    private fun applyToDsl(joinCriteria: JoinReceiver, applyJoin: D.(JoinCollector) -> Unit) {
        getDsl().applyJoin(JoinCollector().apply(joinCriteria))
    }

    private fun applyToDsl(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver,
        applyJoin: D.(KotlinQualifiedSubQueryBuilder, JoinCollector) -> Unit
    ) {
        getDsl().applyJoin(KotlinQualifiedSubQueryBuilder().apply(subQuery), JoinCollector().apply(joinCriteria))
    }
}
