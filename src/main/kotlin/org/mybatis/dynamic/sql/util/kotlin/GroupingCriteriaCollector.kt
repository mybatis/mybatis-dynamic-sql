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
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.ExistsCriterion
import org.mybatis.dynamic.sql.NotCriterion
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.VisitableCondition

typealias GroupingCriteriaReceiver = GroupingCriteriaCollector.() -> Unit

/**
 * This class is used to gather criteria for a where clause. The class gathers two types of criteria:
 * an initial criterion, and sub-criteria connected by either an "and" or an "or".
 *
 * An initial criterion can be one of four types:
 * - A column and condition (called with the invoke operator on a column)
 * - An exists operator (called with the "exists" function)
 * - A criteria group which is essentially parenthesis within the where clause (called with the group function)
 * - A "not" group (called with the not function)
 *
 * Only one of these initial criterion functions should be called within each scope. If you need more than one,
 * use a sub-criteria joined with "and" or "or"
 */
@Suppress("TooManyFunctions")
@MyBatisDslMarker
class GroupingCriteriaCollector {
    internal var initialCriterion: SqlCriterion? = null
        private set(value) {
            if (field != null) {
                throw DuplicateInitialCriterionException()
            }
            field = value
        }

    internal val subCriteria = mutableListOf<AndOrCriteriaGroup>()

    fun and(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.subCriteria.add(
                AndOrCriteriaGroup.Builder().withConnector("and")
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build()
            )
        }

    fun or(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.subCriteria.add(
                AndOrCriteriaGroup.Builder().withConnector("or")
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build()
            )
        }

    /**
     * This should only be specified once per scope, and should be first
     */
    fun not(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.initialCriterion = NotCriterion.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }

    /**
     * This should only be specified once per scope, and should be first
     */
    fun exists(kotlinSubQueryBuilder: KotlinSubQueryBuilder.() -> Unit): Unit =
        with(KotlinSubQueryBuilder().apply(kotlinSubQueryBuilder)) {
            this@GroupingCriteriaCollector.initialCriterion =
                ExistsCriterion.Builder().withExistsPredicate(SqlBuilder.exists(this)).build()
        }

    /**
     * This could "almost" be an operator invoke function. The problem is that
     * to call it a user would need to use "this" explicitly. I think that is too
     * confusing, so we'll stick with the function name of "group"
     *
     * This should only be specified once per scope, and should be first
     */
    fun group(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.initialCriterion = CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }

    /**
     * Build an initial criterion for a where clause, or a nested and/or/not group.
     * You can use it like A (isEqualTo(3))
     */
    operator fun <T> BindableColumn<T>.invoke(condition: VisitableCondition<T>) {
        initialCriterion = ColumnAndConditionCriterion.withColumn(this)
            .withCondition(condition)
            .build()
    }

    // infix functions...we may be able to rewrite these as extension functions once Kotlin solves the multiple
    // receivers problem (https://youtrack.jetbrains.com/issue/KT-42435)

    // conditions for all data types
    fun <T> BindableColumn<T>.isNull() = invoke(SqlBuilder.isNull())

    fun <T> BindableColumn<T>.isNotNull() = invoke(SqlBuilder.isNotNull())

    infix fun <T : Any> BindableColumn<T>.isEqualTo(value: T) = invoke(SqlBuilder.isEqualTo(value))

    infix fun <T> BindableColumn<T>.isEqualToSubQuery(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isEqualTo(KotlinSubQueryBuilder().apply(subQuery)))

    infix fun <T> BindableColumn<T>.isEqualTo(column: BasicColumn) = invoke(SqlBuilder.isEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isEqualToWhenPresent(value: T?) =
        invoke(SqlBuilder.isEqualToWhenPresent<T>(value))

    infix fun <T : Any> BindableColumn<T>.isNotEqualTo(value: T) = invoke(SqlBuilder.isNotEqualTo(value))

    infix fun <T> BindableColumn<T>.isNotEqualToSubQuery(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isNotEqualTo(KotlinSubQueryBuilder().apply(subQuery)))

    infix fun <T> BindableColumn<T>.isNotEqualTo(column: BasicColumn) = invoke(SqlBuilder.isNotEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isNotEqualToWhenPresent(value: T?) =
        invoke(SqlBuilder.isNotEqualToWhenPresent<T>(value))

    infix fun <T : Any> BindableColumn<T>.isGreaterThan(value: T) = invoke(SqlBuilder.isGreaterThan(value))

    infix fun <T> BindableColumn<T>.isGreaterThanSubQuery(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isGreaterThan(KotlinSubQueryBuilder().apply(subQuery)))

    infix fun <T> BindableColumn<T>.isGreaterThan(column: BasicColumn) = invoke(SqlBuilder.isGreaterThan(column))

    infix fun <T : Any> BindableColumn<T>.isGreaterThanWhenPresent(value: T?) =
        invoke(SqlBuilder.isGreaterThanWhenPresent<T>(value))

    infix fun <T : Any> BindableColumn<T>.isGreaterThanOrEqualTo(value: T) =
        invoke(SqlBuilder.isGreaterThanOrEqualTo(value))

    infix fun <T> BindableColumn<T>.isGreaterThanOrEqualToSubQuery(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isGreaterThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery)))

    infix fun <T> BindableColumn<T>.isGreaterThanOrEqualTo(column: BasicColumn) =
        invoke(SqlBuilder.isGreaterThanOrEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isGreaterThanOrEqualToWhenPresent(value: T?) =
        invoke(SqlBuilder.isGreaterThanOrEqualToWhenPresent<T>(value))

    infix fun <T : Any> BindableColumn<T>.isLessThan(value: T) = invoke(SqlBuilder.isLessThan(value))

    infix fun <T> BindableColumn<T>.isLessThanSubQuery(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isLessThan(KotlinSubQueryBuilder().apply(subQuery)))

    infix fun <T> BindableColumn<T>.isLessThan(column: BasicColumn) = invoke(SqlBuilder.isLessThan(column))

    infix fun <T : Any> BindableColumn<T>.isLessThanWhenPresent(value: T?) =
        invoke(SqlBuilder.isLessThanWhenPresent<T>(value))

    infix fun <T : Any> BindableColumn<T>.isLessThanOrEqualTo(value: T) = invoke(SqlBuilder.isLessThanOrEqualTo(value))

    infix fun <T> BindableColumn<T>.isLessThanOrEqualToSubQuery(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isLessThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery)))

    infix fun <T> BindableColumn<T>.isLessThanOrEqualTo(column: BasicColumn) =
        invoke(SqlBuilder.isLessThanOrEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isLessThanOrEqualToWhenPresent(value: T?) =
        invoke(SqlBuilder.isLessThanOrEqualToWhenPresent<T>(value))

    fun <T : Any> BindableColumn<T>.isIn(vararg values: T) = isIn(values.asList())

    infix fun <T : Any> BindableColumn<T>.isIn(values: Collection<T>) = invoke(SqlBuilder.isIn(values))

    infix fun <T> BindableColumn<T>.isIn(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isIn(KotlinSubQueryBuilder().apply(subQuery)))

    fun <T : Any> BindableColumn<T>.isInWhenPresent(vararg values: T?) = isInWhenPresent(values.asList())

    infix fun <T : Any> BindableColumn<T>.isInWhenPresent(values: Collection<T?>?) =
        invoke(SqlBuilder.isInWhenPresent<T>(values))

    fun <T : Any> BindableColumn<T>.isNotIn(vararg values: T) = isNotIn(values.asList())

    infix fun <T : Any> BindableColumn<T>.isNotIn(values: Collection<T>) = invoke(SqlBuilder.isNotIn(values))

    infix fun <T> BindableColumn<T>.isNotIn(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(SqlBuilder.isNotIn(KotlinSubQueryBuilder().apply(subQuery)))

    fun <T : Any> BindableColumn<T>.isNotInWhenPresent(vararg values: T?) = isNotInWhenPresent(values.asList())

    infix fun <T : Any> BindableColumn<T>.isNotInWhenPresent(values: Collection<T?>?) =
        invoke(SqlBuilder.isNotInWhenPresent<T>(values))

    infix fun <T : Any> BindableColumn<T>.isBetween(value1: T) =
        InfixBetweenBuilder(value1) { invoke(it) }

    infix fun <T : Any> BindableColumn<T>.isBetweenWhenPresent(value1: T?) =
        InfixBetweenWhenPresentBuilder(value1) { invoke(it) }

    infix fun <T : Any> BindableColumn<T>.isNotBetween(value1: T) =
        InfixNotBetweenBuilder(value1) { invoke(it) }

    infix fun <T : Any> BindableColumn<T>.isNotBetweenWhenPresent(value1: T?) =
        InfixNotBetweenWhenPresentBuilder(value1) { invoke(it) }

    // for string columns, but generic for columns with type handlers
    infix fun <T : Any> BindableColumn<T>.isLike(value: T) = invoke(SqlBuilder.isLike(value))

    infix fun <T : Any> BindableColumn<T>.isLikeWhenPresent(value: T?) =
        invoke(SqlBuilder.isLikeWhenPresent<T>(value))

    infix fun <T : Any> BindableColumn<T>.isNotLike(value: T) = invoke(SqlBuilder.isNotLike(value))

    infix fun <T : Any> BindableColumn<T>.isNotLikeWhenPresent(value: T?) =
        invoke(SqlBuilder.isNotLikeWhenPresent<T>(value))

    // shortcuts for booleans
    fun BindableColumn<Boolean>.isTrue() = isEqualTo(true)

    fun BindableColumn<Boolean>.isFalse() = isEqualTo(false)

    // conditions for strings only
    infix fun BindableColumn<String>.isLikeCaseInsensitive(value: String) =
        invoke(SqlBuilder.isLikeCaseInsensitive(value))

    infix fun BindableColumn<String>.isLikeCaseInsensitiveWhenPresent(value: String?) =
        invoke(SqlBuilder.isLikeCaseInsensitiveWhenPresent(value))

    infix fun BindableColumn<String>.isNotLikeCaseInsensitive(value: String) =
        invoke(SqlBuilder.isNotLikeCaseInsensitive(value))

    infix fun BindableColumn<String>.isNotLikeCaseInsensitiveWhenPresent(value: String?) =
        invoke(SqlBuilder.isNotLikeCaseInsensitiveWhenPresent(value))

    fun BindableColumn<String>.isInCaseInsensitive(vararg values: String) = isInCaseInsensitive(values.asList())

    infix fun BindableColumn<String>.isInCaseInsensitive(values: Collection<String>) =
        invoke(SqlBuilder.isInCaseInsensitive(values))

    fun BindableColumn<String>.isInCaseInsensitiveWhenPresent(vararg values: String?) =
        isInCaseInsensitiveWhenPresent(values.asList())

    infix fun BindableColumn<String>.isInCaseInsensitiveWhenPresent(values: Collection<String?>?) =
        invoke(SqlBuilder.isInCaseInsensitiveWhenPresent(values))

    fun BindableColumn<String>.isNotInCaseInsensitive(vararg values: String) =
        isNotInCaseInsensitive(values.asList())

    infix fun BindableColumn<String>.isNotInCaseInsensitive(values: Collection<String>) =
        invoke(SqlBuilder.isNotInCaseInsensitive(values))

    fun BindableColumn<String>.isNotInCaseInsensitiveWhenPresent(vararg values: String?) =
        isNotInCaseInsensitiveWhenPresent(values.asList())

    infix fun BindableColumn<String>.isNotInCaseInsensitiveWhenPresent(values: Collection<String?>?) =
        invoke(SqlBuilder.isNotInCaseInsensitiveWhenPresent(values))
}
