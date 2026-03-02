/*
 *    Copyright 2016-2026 the original author or authors.
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
import org.mybatis.dynamic.sql.dsl.JoinOperations
import org.mybatis.dynamic.sql.dsl.WhereOperations
import org.mybatis.dynamic.sql.util.ConfigurableStatement

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

@MyBatisDslMarker
abstract class KotlinWhereOperations<D>(private val dsl : D)
        where D : ConfigurableStatement<*>, D : WhereOperations<*> {
    fun where(criteria: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteria).let {
            dsl.where(it.initialCriterion, it.subCriteria)
        }

    fun where(criteria: List<AndOrCriteriaGroup>) {
        dsl.where(criteria)
    }

    /**
     * This function does nothing, but it can be used to make some code snippets more understandable.
     *
     * For example, to count all rows in a table, you can write either of the following:
     *
     * ```kotlin
     * val rows = countFrom(foo) { }
     * ```
     * or
     * ```kotlin
     * val rows = countFrom(foo) { allRows() }
     * ```
     */
    @SuppressWarnings("EmptyFunctionBlock")
    fun allRows() {
        // intentionally empty - this function exists for code beautification and clarity only
    }

    fun configureStatement(c: StatementConfiguration.() -> Unit) {
        dsl.configureStatement(c)
    }
}

@Suppress("TooManyFunctions")
abstract class KotlinJoinOperations<D>(private val dsl : D) : KotlinWhereOperations<D>(dsl)
        where D : JoinOperations<*,*>, D: ConfigurableStatement<*>, D: WhereOperations<*>{
    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun join(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            join(table, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun join(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            join(table, alias, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun join(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            join(sq, sq.correlationName, jc.initialCriterion(), jc.subCriteria())
        }

    fun join(table: SqlTable): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.join(table).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun join(table: SqlTable, alias: String): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.join(table, alias).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun join(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            val sq = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            dsl.join(sq, sq.correlationName).on(it.initialCriterion!!).and(it.subCriteria)
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun fullJoin(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun fullJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, alias, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun fullJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            fullJoin(sq, sq.correlationName, jc.initialCriterion(), jc.subCriteria())
        }

    fun fullJoin(table: SqlTable): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.fullJoin(table).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun fullJoin(table: SqlTable, alias: String): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.fullJoin(table, alias).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun fullJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            val sq = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            dsl.fullJoin(sq, sq.correlationName).on(it.initialCriterion!!).and(it.subCriteria)
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun leftJoin(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun leftJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, alias, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun leftJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            leftJoin(sq, sq.correlationName, jc.initialCriterion(), jc.subCriteria())
        }

    fun leftJoin(table: SqlTable): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.leftJoin(table).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun leftJoin(table: SqlTable, alias: String): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.leftJoin(table, alias).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun leftJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            val sq = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            dsl.leftJoin(sq, sq.correlationName).on(it.initialCriterion!!).and(it.subCriteria)
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun rightJoin(table: SqlTable, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun rightJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): Unit =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, alias, jc.initialCriterion(), jc.subCriteria())
        }

    @Deprecated("Please use the new form with the \"on\" keyword outside the lambda")
    fun rightJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): Unit =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            rightJoin(sq, sq.correlationName, jc.initialCriterion(), jc.subCriteria())
        }

    fun rightJoin(table: SqlTable): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.rightJoin(table).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun rightJoin(table: SqlTable, alias: String): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            dsl.rightJoin(table, alias).on(it.initialCriterion!!).and(it.subCriteria)
        }

    fun rightJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit): JoinCriteriaGatherer =
        JoinCriteriaGatherer {
            val sq = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            dsl.rightJoin(sq, sq.correlationName).on(it.initialCriterion!!).and(it.subCriteria)
        }

    private fun applyToDsl(joinCriteria: JoinReceiver, applyJoin: D.(JoinCollector) -> Unit) {
        dsl.applyJoin(JoinCollector().apply(joinCriteria))
    }

    private fun applyToDsl(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver,
        applyJoin: D.(KotlinQualifiedSubQueryBuilder, JoinCollector) -> Unit
    ) {
        dsl.applyJoin(KotlinQualifiedSubQueryBuilder().apply(subQuery), JoinCollector().apply(joinCriteria))
    }
}

class JoinCriteriaGatherer(private val consumer: (GroupingCriteriaCollector) -> Unit) {
    infix fun on (joinCriteria: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(joinCriteria)) {
            assertTrue(initialCriterion != null, "ERROR.22") //$NON-NLS-1$
            consumer.invoke(this)
        }
}
