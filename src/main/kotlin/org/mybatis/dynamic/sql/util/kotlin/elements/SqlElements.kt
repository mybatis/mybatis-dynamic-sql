/*
 *    Copyright 2016-2025 the original author or authors.
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
@file:Suppress("TooManyFunctions")
package org.mybatis.dynamic.sql.util.kotlin.elements

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.BoundValue
import org.mybatis.dynamic.sql.Constant
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.StringConstant
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseModel
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseModel
import org.mybatis.dynamic.sql.select.aggregate.Avg
import org.mybatis.dynamic.sql.select.aggregate.Count
import org.mybatis.dynamic.sql.select.aggregate.CountAll
import org.mybatis.dynamic.sql.select.aggregate.CountDistinct
import org.mybatis.dynamic.sql.select.aggregate.Max
import org.mybatis.dynamic.sql.select.aggregate.Min
import org.mybatis.dynamic.sql.select.aggregate.Sum
import org.mybatis.dynamic.sql.select.function.Add
import org.mybatis.dynamic.sql.select.function.Cast
import org.mybatis.dynamic.sql.select.function.Concat
import org.mybatis.dynamic.sql.select.function.Concatenate
import org.mybatis.dynamic.sql.select.function.Divide
import org.mybatis.dynamic.sql.select.function.Lower
import org.mybatis.dynamic.sql.select.function.Multiply
import org.mybatis.dynamic.sql.select.function.OperatorFunction
import org.mybatis.dynamic.sql.select.function.Substring
import org.mybatis.dynamic.sql.select.function.Subtract
import org.mybatis.dynamic.sql.select.function.Upper
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaCollector
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaReceiver
import org.mybatis.dynamic.sql.util.kotlin.KotlinSubQueryBuilder
import org.mybatis.dynamic.sql.util.kotlin.invalidIfNull
import org.mybatis.dynamic.sql.where.condition.IsBetween
import org.mybatis.dynamic.sql.where.condition.IsBetweenWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsEqualTo
import org.mybatis.dynamic.sql.where.condition.IsEqualToColumn
import org.mybatis.dynamic.sql.where.condition.IsEqualToWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsGreaterThan
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanColumn
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualTo
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToColumn
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsIn
import org.mybatis.dynamic.sql.where.condition.IsInCaseInsensitive
import org.mybatis.dynamic.sql.where.condition.IsInCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsInWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsInWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsLessThan
import org.mybatis.dynamic.sql.where.condition.IsLessThanColumn
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualTo
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToColumn
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsLessThanWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsLessThanWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsLike
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitive
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsLikeWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotBetween
import org.mybatis.dynamic.sql.where.condition.IsNotBetweenWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotEqualTo
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToColumn
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsNotIn
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitive
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotInWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotInWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsNotLike
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitive
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotLikeWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotNull
import org.mybatis.dynamic.sql.where.condition.IsNull

// support for criteria without initial conditions
fun and(receiver: GroupingCriteriaReceiver): AndOrCriteriaGroup =
    with(GroupingCriteriaCollector().apply(receiver)) {
        AndOrCriteriaGroup.Builder().withInitialCriterion(initialCriterion)
            .withSubCriteria(subCriteria)
            .withConnector("and")
            .build()
    }

fun or(receiver: GroupingCriteriaReceiver): AndOrCriteriaGroup =
    with(GroupingCriteriaCollector().apply(receiver)) {
        AndOrCriteriaGroup.Builder().withInitialCriterion(initialCriterion)
            .withSubCriteria(subCriteria)
            .withConnector("or")
            .build()
    }

// case expressions
fun case(dslCompleter: KSearchedCaseDSL.() -> Unit): SearchedCaseModel =
    KSearchedCaseDSL().apply(dslCompleter).run {
        SearchedCaseModel.Builder()
            .withWhenConditions(whenConditions)
            .withElseValue(elseValue)
            .build()
    }

fun <T : Any> case(column: BindableColumn<T>, dslCompleter: KSimpleCaseDSL<T>.() -> Unit) : SimpleCaseModel<T> =
    KSimpleCaseDSL<T>().apply(dslCompleter).run {
        SimpleCaseModel.Builder<T>()
            .withColumn(column)
            .withWhenConditions(whenConditions)
            .withElseValue(elseValue)
            .build()
    }

// aggregate support
fun count(): CountAll = SqlBuilder.count()

fun count(column: BasicColumn): Count = SqlBuilder.count(column)

fun countDistinct(column: BasicColumn): CountDistinct = SqlBuilder.countDistinct(column)

fun <T : Any> max(column: BindableColumn<T>): Max<T> = SqlBuilder.max(column)

fun <T : Any> min(column: BindableColumn<T>): Min<T> = SqlBuilder.min(column)

fun <T : Any> avg(column: BindableColumn<T>): Avg<T> = SqlBuilder.avg(column)

fun <T : Any> sum(column: BindableColumn<T>): Sum<T> = SqlBuilder.sum(column)

fun sum(column: BasicColumn): Sum<*> = SqlBuilder.sum(column)

fun <T : Any> sum(column: BindableColumn<T>, condition: RenderableCondition<T>): Sum<T> = SqlBuilder.sum(column, condition)

// constants
fun <T : Any> constant(constant: String): Constant<T> = SqlBuilder.constant(constant)

fun stringConstant(constant: String): StringConstant = SqlBuilder.stringConstant(constant)

fun <T : Any> value(value: T): BoundValue<T> = SqlBuilder.value(value)

// functions
fun <T : Any> add(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Add<T> = Add.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Any> divide(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Divide<T> = Divide.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Any> multiply(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Multiply<T> = Multiply.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Any> subtract(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Subtract<T> = Subtract.of(firstColumn, secondColumn, subsequentColumns.asList())

fun cast(receiver: CastDSL.() -> Unit): Cast =
    invalidIfNull(CastDSL().apply(receiver).cast, "ERROR.43")

fun <T : Any> concat(
    firstColumn: BindableColumn<T>,
    vararg subsequentColumns: BasicColumn
): Concat<T> = Concat.of(firstColumn, subsequentColumns.asList())

fun <T : Any> concatenate(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Concatenate<T> = Concatenate.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Any> applyOperator(
    operator: String,
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): OperatorFunction<T> = OperatorFunction.of(operator, firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Any> lower(column: BindableColumn<T>): Lower<T> = SqlBuilder.lower(column)

fun <T : Any> substring(
    column: BindableColumn<T>,
    offset: Int,
    length: Int
): Substring<T> = SqlBuilder.substring(column, offset, length)

fun <T : Any> upper(column: BindableColumn<T>): Upper<T> = SqlBuilder.upper(column)

// conditions for all data types
fun <T : Any> isNull(): IsNull<T> = SqlBuilder.isNull()

fun <T : Any> isNotNull(): IsNotNull<T> = SqlBuilder.isNotNull()

fun <T : Any> isEqualTo(value: T): IsEqualTo<T> = SqlBuilder.isEqualTo(value)

fun <T : Any> isEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsEqualToWithSubselect<T> =
    SqlBuilder.isEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isEqualTo(column: BasicColumn): IsEqualToColumn<T> = SqlBuilder.isEqualTo(column)

fun <T : Any> isEqualToWhenPresent(value: T?): IsEqualToWhenPresent<T> = SqlBuilder.isEqualToWhenPresent(value)

fun <T : Any> isNotEqualTo(value: T): IsNotEqualTo<T> = SqlBuilder.isNotEqualTo(value)

fun <T : Any> isNotEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotEqualToWithSubselect<T> =
    SqlBuilder.isNotEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isNotEqualTo(column: BasicColumn): IsNotEqualToColumn<T> = SqlBuilder.isNotEqualTo(column)

fun <T : Any> isNotEqualToWhenPresent(value: T?): IsNotEqualToWhenPresent<T> =
    SqlBuilder.isNotEqualToWhenPresent(value)

fun <T : Any> isGreaterThan(value: T): IsGreaterThan<T> = SqlBuilder.isGreaterThan(value)

fun <T : Any> isGreaterThan(subQuery: KotlinSubQueryBuilder.() -> Unit): IsGreaterThanWithSubselect<T> =
    SqlBuilder.isGreaterThan(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isGreaterThan(column: BasicColumn): IsGreaterThanColumn<T> = SqlBuilder.isGreaterThan(column)

fun <T : Any> isGreaterThanWhenPresent(value: T?): IsGreaterThanWhenPresent<T> =
    SqlBuilder.isGreaterThanWhenPresent(value)

fun <T : Any> isGreaterThanOrEqualTo(value: T): IsGreaterThanOrEqualTo<T> = SqlBuilder.isGreaterThanOrEqualTo(value)

fun <T : Any> isGreaterThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsGreaterThanOrEqualToWithSubselect<T> =
    SqlBuilder.isGreaterThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isGreaterThanOrEqualTo(column: BasicColumn): IsGreaterThanOrEqualToColumn<T> =
    SqlBuilder.isGreaterThanOrEqualTo(column)

fun <T : Any> isGreaterThanOrEqualToWhenPresent(value: T?): IsGreaterThanOrEqualToWhenPresent<T> =
    SqlBuilder.isGreaterThanOrEqualToWhenPresent(value)

fun <T : Any> isLessThan(value: T): IsLessThan<T> = SqlBuilder.isLessThan(value)

fun <T : Any> isLessThan(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanWithSubselect<T> =
    SqlBuilder.isLessThan(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isLessThan(column: BasicColumn): IsLessThanColumn<T> = SqlBuilder.isLessThan(column)

fun <T : Any> isLessThanWhenPresent(value: T?): IsLessThanWhenPresent<T> = SqlBuilder.isLessThanWhenPresent(value)

fun <T : Any> isLessThanOrEqualTo(value: T): IsLessThanOrEqualTo<T> = SqlBuilder.isLessThanOrEqualTo(value)

fun <T : Any> isLessThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanOrEqualToWithSubselect<T> =
    SqlBuilder.isLessThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isLessThanOrEqualTo(column: BasicColumn): IsLessThanOrEqualToColumn<T> = SqlBuilder.isLessThanOrEqualTo(column)

fun <T : Any> isLessThanOrEqualToWhenPresent(value: T?): IsLessThanOrEqualToWhenPresent<T> =
    SqlBuilder.isLessThanOrEqualToWhenPresent(value)

fun <T : Any> isIn(vararg values: T): IsIn<T> = isIn(values.asList())

@JvmName("isInArray")
fun <T : Any> isIn(values: Array<out T>): IsIn<T> = SqlBuilder.isIn(values.asList())

fun <T : Any> isIn(values: Collection<T>): IsIn<T> = SqlBuilder.isIn(values)

fun <T : Any> isIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsInWithSubselect<T> =
    SqlBuilder.isIn(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isInWhenPresent(vararg values: T?): IsInWhenPresent<T> = isInWhenPresent(values.asList())

@JvmName("isInArrayWhenPresent")
fun <T : Any> isInWhenPresent(values: Array<out T?>?): IsInWhenPresent<T> = SqlBuilder.isInWhenPresent(values?.asList())

fun <T : Any> isInWhenPresent(values: Collection<T?>?): IsInWhenPresent<T> = SqlBuilder.isInWhenPresent(values)

fun <T : Any> isNotIn(vararg values: T): IsNotIn<T> = isNotIn(values.asList())

@JvmName("isNotInArray")
fun <T : Any> isNotIn(values: Array<out T>): IsNotIn<T> = SqlBuilder.isNotIn(values.asList())

fun <T : Any> isNotIn(values: Collection<T>): IsNotIn<T> = SqlBuilder.isNotIn(values)

fun <T : Any> isNotIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotInWithSubselect<T> =
    SqlBuilder.isNotIn(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isNotInWhenPresent(vararg values: T?): IsNotInWhenPresent<T> = isNotInWhenPresent(values.asList())

@JvmName("isNotInArrayWhenPresent")
fun <T : Any> isNotInWhenPresent(values: Array<out T?>?): IsNotInWhenPresent<T> = SqlBuilder.isNotInWhenPresent(values?.asList())

fun <T : Any> isNotInWhenPresent(values: Collection<T?>?): IsNotInWhenPresent<T> = SqlBuilder.isNotInWhenPresent(values)

fun <T : Any> isBetween(value1: T): BetweenBuilder<T> = BetweenBuilder(value1)

fun <T : Any> isBetweenWhenPresent(value1: T?): BetweenWhenPresentBuilder<T> = BetweenWhenPresentBuilder(value1)

fun <T : Any> isNotBetween(value1: T): NotBetweenBuilder<T> = NotBetweenBuilder(value1)

fun <T : Any> isNotBetweenWhenPresent(value1: T?): NotBetweenWhenPresentBuilder<T> =
    NotBetweenWhenPresentBuilder(value1)

// for string columns, but generic for columns with type handlers
fun <T : Any> isLike(value: T): IsLike<T> = SqlBuilder.isLike(value)

fun <T : Any> isLikeWhenPresent(value: T?): IsLikeWhenPresent<T> = SqlBuilder.isLikeWhenPresent(value)

fun <T : Any> isNotLike(value: T): IsNotLike<T> = SqlBuilder.isNotLike(value)

fun <T : Any> isNotLikeWhenPresent(value: T?): IsNotLikeWhenPresent<T> = SqlBuilder.isNotLikeWhenPresent(value)

// shortcuts for booleans
fun isTrue(): IsEqualTo<Boolean> = isEqualTo(true)

fun isFalse(): IsEqualTo<Boolean> = isEqualTo(false)

// conditions for strings only
fun isLikeCaseInsensitive(value: String): IsLikeCaseInsensitive<String> = SqlBuilder.isLikeCaseInsensitive(value)

fun isLikeCaseInsensitiveWhenPresent(value: String?): IsLikeCaseInsensitiveWhenPresent<String> =
    SqlBuilder.isLikeCaseInsensitiveWhenPresent(value)

fun isNotLikeCaseInsensitive(value: String): IsNotLikeCaseInsensitive<String> = SqlBuilder.isNotLikeCaseInsensitive(value)

fun isNotLikeCaseInsensitiveWhenPresent(value: String?): IsNotLikeCaseInsensitiveWhenPresent<String> =
    SqlBuilder.isNotLikeCaseInsensitiveWhenPresent(value)

fun isInCaseInsensitive(vararg values: String): IsInCaseInsensitive<String> = isInCaseInsensitive(values.asList())

@JvmName("isInArrayCaseInsensitive")
fun isInCaseInsensitive(values: Array<out String>): IsInCaseInsensitive<String> =
    SqlBuilder.isInCaseInsensitive(values.asList())

fun isInCaseInsensitive(values: Collection<String>): IsInCaseInsensitive<String> =
    SqlBuilder.isInCaseInsensitive(values)

fun isInCaseInsensitiveWhenPresent(vararg values: String?): IsInCaseInsensitiveWhenPresent<String> =
    isInCaseInsensitiveWhenPresent(values.asList())

@JvmName("isInArrayCaseInsensitiveWhenPresent")
fun isInCaseInsensitiveWhenPresent(values: Array<out String?>?): IsInCaseInsensitiveWhenPresent<String> =
    SqlBuilder.isInCaseInsensitiveWhenPresent(values?.asList())

fun isInCaseInsensitiveWhenPresent(values: Collection<String?>?): IsInCaseInsensitiveWhenPresent<String> =
    SqlBuilder.isInCaseInsensitiveWhenPresent(values)

fun isNotInCaseInsensitive(vararg values: String): IsNotInCaseInsensitive<String> =
    isNotInCaseInsensitive(values.asList())

@JvmName("isNotInArrayCaseInsensitive")
fun isNotInCaseInsensitive(values: Array<out String>): IsNotInCaseInsensitive<String> =
    SqlBuilder.isNotInCaseInsensitive(values.asList())

fun isNotInCaseInsensitive(values: Collection<String>): IsNotInCaseInsensitive<String> =
    SqlBuilder.isNotInCaseInsensitive(values)

fun isNotInCaseInsensitiveWhenPresent(vararg values: String?): IsNotInCaseInsensitiveWhenPresent<String> =
    isNotInCaseInsensitiveWhenPresent(values.asList())

@JvmName("isNotInArrayCaseInsensitiveWhenPresent")
fun isNotInCaseInsensitiveWhenPresent(values: Array<out String?>?): IsNotInCaseInsensitiveWhenPresent<String> =
    SqlBuilder.isNotInCaseInsensitiveWhenPresent(values?.asList())

fun isNotInCaseInsensitiveWhenPresent(values: Collection<String?>?): IsNotInCaseInsensitiveWhenPresent<String> =
    SqlBuilder.isNotInCaseInsensitiveWhenPresent(values)

// order by support
/**
 * Creates a sort specification based on a String. This is useful when a column has been
 * aliased in the select list.
 *
 * @param name the string to use as a sort specification
 * @return a sort specification
 */
fun sortColumn(name: String): SortSpecification = SqlBuilder.sortColumn(name)

/**
 * Creates a sort specification based on a column and a table alias. This can be useful in a join
 * where the desired sort order is based on a column not in the select list. This will likely
 * fail in union queries depending on database support.
 *
 * @param tableAlias the table alias
 * @param column the column
 * @return a sort specification
 */
fun sortColumn(tableAlias: String, column: SqlColumn<*>): SortSpecification = SqlBuilder.sortColumn(tableAlias, column)

// DSL Support Classes
class BetweenBuilder<T : Any>(private val value1: T) {
    fun and(value2: T): IsBetween<T> = SqlBuilder.isBetween(value1).and(value2)
}

class BetweenWhenPresentBuilder<T : Any>(private val value1: T?) {
    fun and(value2: T?): IsBetweenWhenPresent<T> {
        return SqlBuilder.isBetweenWhenPresent<T>(value1).and(value2)
    }
}

class NotBetweenBuilder<T : Any>(private val value1: T) {
    fun and(value2: T): IsNotBetween<T> = SqlBuilder.isNotBetween(value1).and(value2)
}

class NotBetweenWhenPresentBuilder<T : Any>(private val value1: T?) {
    fun and(value2: T?): IsNotBetweenWhenPresent<T> {
        return SqlBuilder.isNotBetweenWhenPresent<T>(value1).and(value2)
    }
}
