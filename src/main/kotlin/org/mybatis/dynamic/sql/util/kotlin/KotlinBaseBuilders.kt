/*
 *    Copyright 2016-2022 the original author or authors.
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
import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.ExistsPredicate
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.AbstractQueryExpressionDSL
import org.mybatis.dynamic.sql.where.AbstractWhereDSL
import org.mybatis.dynamic.sql.where.AbstractWhereSupport

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

typealias WhereApplier = KotlinBaseBuilder<*, *>.() -> Unit

fun WhereApplier.andThen(after: WhereApplier): WhereApplier = {
    invoke(this)
    after(this)
}

@MyBatisDslMarker
@Suppress("TooManyFunctions")
abstract class KotlinBaseBuilder<D : AbstractWhereSupport<*>, B : KotlinBaseBuilder<D, B>> {
    fun where(criteria: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteria)) {
            this@KotlinBaseBuilder.getDsl().where(initialCriterion, subCriteria)
        }

    fun and(criteria: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteria)) {
            this@KotlinBaseBuilder.getDsl().where().and(initialCriterion, subCriteria)
        }

    fun or(criteria: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteria)) {
            this@KotlinBaseBuilder.getDsl().where().or(initialCriterion, subCriteria)
        }

    fun applyWhere(whereApplier: WhereApplier): B =
        self().apply {
            whereApplier.invoke(this)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applyToWhere {
            where(column, condition)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applyToWhere(subCriteria) { sc ->
            where(column, condition, sc)
        }

    @Deprecated(
        message = "Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.",
        replaceWith = ReplaceWith("where { existsPredicate }")
    )
    fun where(existsPredicate: ExistsPredicate): B =
        applyToWhere {
            where(existsPredicate)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun where(existsPredicate: ExistsPredicate, subCriteria: CriteriaReceiver): B =
        applyToWhere(subCriteria) { sc ->
            where(existsPredicate, sc)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applyToWhere {
            and(column, condition)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applyToWhere(subCriteria) { sc ->
            and(column, condition, sc)
        }

    @Deprecated(
        message = "Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.",
        replaceWith = ReplaceWith("and { existsPredicate }")
    )
    fun and(existsPredicate: ExistsPredicate): B =
        applyToWhere {
            and(existsPredicate)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun and(existsPredicate: ExistsPredicate, subCriteria: CriteriaReceiver): B =
        applyToWhere(subCriteria) { sc ->
            and(existsPredicate, sc)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applyToWhere {
            or(column, condition)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, subCriteria: CriteriaReceiver): B =
        applyToWhere(subCriteria) { sc ->
            or(column, condition, sc)
        }

    @Deprecated(
        message = "Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.",
        replaceWith = ReplaceWith("or { existsPredicate }")
    )
    fun or(existsPredicate: ExistsPredicate): B =
        applyToWhere {
            or(existsPredicate)
        }

    @Deprecated("Deprecated in favor of the new where clause DSL. Please see the documentation for new usage.")
    fun or(existsPredicate: ExistsPredicate, subCriteria: CriteriaReceiver): B =
        applyToWhere(subCriteria) { sc ->
            or(existsPredicate, sc)
        }

    fun allRows(): B = self()

    private fun applyToWhere(block: AbstractWhereDSL<*>.() -> Unit): B {
        getDsl().where().apply(block)
        return self()
    }

    private fun applyToWhere(
        subCriteria: CriteriaReceiver,
        block: AbstractWhereDSL<*>.(List<AndOrCriteriaGroup>) -> Unit
    ): B {
        getDsl().where().block(CriteriaCollector().apply(subCriteria).criteria)
        return self()
    }

    protected abstract fun self(): B

    protected abstract fun getDsl(): D
}

@Suppress("TooManyFunctions")
abstract class KotlinBaseJoiningBuilder<D : AbstractQueryExpressionDSL<*, *>, B : KotlinBaseJoiningBuilder<D, B>> :
    KotlinBaseBuilder<D, B>() {

    fun join(table: SqlTable, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            join(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun join(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            join(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun join(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            join(sq, sq.correlationName, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun fullJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            fullJoin(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun fullJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            fullJoin(sq, sq.correlationName, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun leftJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            leftJoin(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun leftJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
        applyToDsl(subQuery, joinCriteria) { sq, jc ->
            leftJoin(sq, sq.correlationName, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun rightJoin(table: SqlTable, alias: String, joinCriteria: JoinReceiver): B =
        applyToDsl(joinCriteria) { jc ->
            rightJoin(table, alias, jc.onJoinCriterion, jc.andJoinCriteria)
        }

    fun rightJoin(
        subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit,
        joinCriteria: JoinReceiver
    ): B =
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
