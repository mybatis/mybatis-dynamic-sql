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
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.StringConstant
import org.mybatis.dynamic.sql.VisitableCondition
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
import org.mybatis.dynamic.sql.where.condition.IsEqualTo
import org.mybatis.dynamic.sql.where.condition.IsEqualToColumn
import org.mybatis.dynamic.sql.where.condition.IsEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsGreaterThan
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanColumn
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualTo
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToColumn
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWithSubselect
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
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsLessThanWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsLike
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitive
import org.mybatis.dynamic.sql.where.condition.IsNotBetween
import org.mybatis.dynamic.sql.where.condition.IsNotEqualTo
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToColumn
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsNotIn
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitive
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotInWhenPresent
import org.mybatis.dynamic.sql.where.condition.IsNotInWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsNotLike
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitive
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

fun <T> max(column: BindableColumn<T>): Max<T> = SqlBuilder.max(column)

fun <T> min(column: BindableColumn<T>): Min<T> = SqlBuilder.min(column)

fun <T> avg(column: BindableColumn<T>): Avg<T> = SqlBuilder.avg(column)

fun <T> sum(column: BindableColumn<T>): Sum<T> = SqlBuilder.sum(column)

fun sum(column: BasicColumn): Sum<*> = SqlBuilder.sum(column)

fun <T> sum(column: BindableColumn<T>, condition: VisitableCondition<T>): Sum<T> = SqlBuilder.sum(column, condition)

// constants
fun <T> constant(constant: String): Constant<T> = SqlBuilder.constant(constant)

fun stringConstant(constant: String): StringConstant = SqlBuilder.stringConstant(constant)

fun <T> value(value: T): BoundValue<T> = SqlBuilder.value(value)

// functions
fun <T> add(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Add<T> = Add.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T> divide(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Divide<T> = Divide.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T> multiply(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Multiply<T> = Multiply.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T> subtract(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Subtract<T> = Subtract.of(firstColumn, secondColumn, subsequentColumns.asList())

fun cast(receiver: CastDSL.() -> Unit): Cast =
    invalidIfNull(CastDSL().apply(receiver).cast, "ERROR.43")

fun <T> concat(
    firstColumn: BindableColumn<T>,
    vararg subsequentColumns: BasicColumn
): Concat<T> = Concat.of(firstColumn, subsequentColumns.asList())

fun <T> concatenate(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Concatenate<T> = Concatenate.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T> applyOperator(
    operator: String,
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): OperatorFunction<T> = OperatorFunction.of(operator, firstColumn, secondColumn, subsequentColumns.asList())

fun <T> lower(column: BindableColumn<T>): Lower<T> = SqlBuilder.lower(column)

fun <T> substring(
    column: BindableColumn<T>,
    offset: Int,
    length: Int
): Substring<T> = SqlBuilder.substring(column, offset, length)

fun <T> upper(column: BindableColumn<T>): Upper<T> = SqlBuilder.upper(column)

// conditions for all data types
fun <T> isNull(): IsNull<T> = SqlBuilder.isNull()

fun <T> isNotNull(): IsNotNull<T> = SqlBuilder.isNotNull()

fun <T> isEqualTo(value: T & Any): IsEqualTo<T> = SqlBuilder.isEqualTo(value)

fun <T> isEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsEqualToWithSubselect<T> =
    SqlBuilder.isEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isEqualTo(column: BasicColumn): IsEqualToColumn<T> = SqlBuilder.isEqualTo(column)

fun <T> isEqualToWhenPresent(value: T?): IsEqualTo<T> = SqlBuilder.isEqualToWhenPresent(value)

fun <T> isNotEqualTo(value: T & Any): IsNotEqualTo<T> = SqlBuilder.isNotEqualTo(value)

fun <T> isNotEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotEqualToWithSubselect<T> =
    SqlBuilder.isNotEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isNotEqualTo(column: BasicColumn): IsNotEqualToColumn<T> = SqlBuilder.isNotEqualTo(column)

fun <T> isNotEqualToWhenPresent(value: T?): IsNotEqualTo<T> = SqlBuilder.isNotEqualToWhenPresent(value)

fun <T> isGreaterThan(value: T & Any): IsGreaterThan<T> = SqlBuilder.isGreaterThan(value)

fun <T> isGreaterThan(subQuery: KotlinSubQueryBuilder.() -> Unit): IsGreaterThanWithSubselect<T> =
    SqlBuilder.isGreaterThan(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isGreaterThan(column: BasicColumn): IsGreaterThanColumn<T> = SqlBuilder.isGreaterThan(column)

fun <T> isGreaterThanWhenPresent(value: T?): IsGreaterThan<T> = SqlBuilder.isGreaterThanWhenPresent(value)

fun <T> isGreaterThanOrEqualTo(value: T & Any): IsGreaterThanOrEqualTo<T> = SqlBuilder.isGreaterThanOrEqualTo(value)

fun <T> isGreaterThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsGreaterThanOrEqualToWithSubselect<T> =
    SqlBuilder.isGreaterThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isGreaterThanOrEqualTo(column: BasicColumn): IsGreaterThanOrEqualToColumn<T> =
    SqlBuilder.isGreaterThanOrEqualTo(column)

fun <T> isGreaterThanOrEqualToWhenPresent(value: T?): IsGreaterThanOrEqualTo<T> =
    SqlBuilder.isGreaterThanOrEqualToWhenPresent(value)

fun <T> isLessThan(value: T & Any): IsLessThan<T> = SqlBuilder.isLessThan(value)

fun <T> isLessThan(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanWithSubselect<T> =
    SqlBuilder.isLessThan(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isLessThan(column: BasicColumn): IsLessThanColumn<T> = SqlBuilder.isLessThan(column)

fun <T> isLessThanWhenPresent(value: T?): IsLessThan<T> = SqlBuilder.isLessThanWhenPresent(value)

fun <T> isLessThanOrEqualTo(value: T & Any): IsLessThanOrEqualTo<T> = SqlBuilder.isLessThanOrEqualTo(value)

fun <T> isLessThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanOrEqualToWithSubselect<T> =
    SqlBuilder.isLessThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isLessThanOrEqualTo(column: BasicColumn): IsLessThanOrEqualToColumn<T> = SqlBuilder.isLessThanOrEqualTo(column)

fun <T> isLessThanOrEqualToWhenPresent(value: T?): IsLessThanOrEqualTo<T> =
    SqlBuilder.isLessThanOrEqualToWhenPresent(value)

fun <T> isIn(vararg values: T & Any): IsIn<T> = isIn(values.asList())

@JvmName("isInArray")
fun <T> isIn(values: Array<out T & Any>): IsIn<T> = SqlBuilder.isIn(values.asList())

fun <T> isIn(values: Collection<T & Any>): IsIn<T> = SqlBuilder.isIn(values)

fun <T> isIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsInWithSubselect<T> =
    SqlBuilder.isIn(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isInWhenPresent(vararg values: T?): IsInWhenPresent<T> = isInWhenPresent(values.asList())

@JvmName("isInArrayWhenPresent")
fun <T> isInWhenPresent(values: Array<out T?>?): IsInWhenPresent<T> = SqlBuilder.isInWhenPresent(values?.asList())

fun <T> isInWhenPresent(values: Collection<T?>?): IsInWhenPresent<T> = SqlBuilder.isInWhenPresent(values)

fun <T> isNotIn(vararg values: T & Any): IsNotIn<T> = isNotIn(values.asList())

@JvmName("isNotInArray")
fun <T> isNotIn(values: Array<out T & Any>): IsNotIn<T> = SqlBuilder.isNotIn(values.asList())

fun <T> isNotIn(values: Collection<T & Any>): IsNotIn<T> = SqlBuilder.isNotIn(values)

fun <T> isNotIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotInWithSubselect<T> =
    SqlBuilder.isNotIn(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isNotInWhenPresent(vararg values: T?): IsNotInWhenPresent<T> = isNotInWhenPresent(values.asList())

@JvmName("isNotInArrayWhenPresent")
fun <T> isNotInWhenPresent(values: Array<out T?>?): IsNotInWhenPresent<T> = SqlBuilder.isNotInWhenPresent(values?.asList())

fun <T> isNotInWhenPresent(values: Collection<T?>?): IsNotInWhenPresent<T> = SqlBuilder.isNotInWhenPresent(values)

fun <T> isBetween(value1: T & Any): BetweenBuilder<T & Any> = BetweenBuilder(value1)

fun <T> isBetweenWhenPresent(value1: T?): BetweenWhenPresentBuilder<T> = BetweenWhenPresentBuilder(value1)

fun <T> isNotBetween(value1: T & Any): NotBetweenBuilder<T & Any> = NotBetweenBuilder(value1)

fun <T> isNotBetweenWhenPresent(value1: T?): NotBetweenWhenPresentBuilder<T> =
    NotBetweenWhenPresentBuilder(value1)

// for string columns, but generic for columns with type handlers
fun <T> isLike(value: T & Any): IsLike<T> = SqlBuilder.isLike(value)

fun <T> isLikeWhenPresent(value: T?): IsLike<T> = SqlBuilder.isLikeWhenPresent(value)

fun <T> isNotLike(value: T & Any): IsNotLike<T> = SqlBuilder.isNotLike(value)

fun <T> isNotLikeWhenPresent(value: T?): IsNotLike<T> = SqlBuilder.isNotLikeWhenPresent(value)

// shortcuts for booleans
fun isTrue(): IsEqualTo<Boolean> = isEqualTo(true)

fun isFalse(): IsEqualTo<Boolean> = isEqualTo(false)

// conditions for strings only
fun isLikeCaseInsensitive(value: String): IsLikeCaseInsensitive = SqlBuilder.isLikeCaseInsensitive(value)

fun isLikeCaseInsensitiveWhenPresent(value: String?): IsLikeCaseInsensitive =
    SqlBuilder.isLikeCaseInsensitiveWhenPresent(value)

fun isNotLikeCaseInsensitive(value: String): IsNotLikeCaseInsensitive = SqlBuilder.isNotLikeCaseInsensitive(value)

fun isNotLikeCaseInsensitiveWhenPresent(value: String?): IsNotLikeCaseInsensitive =
    SqlBuilder.isNotLikeCaseInsensitiveWhenPresent(value)

fun isInCaseInsensitive(vararg values: String): IsInCaseInsensitive = isInCaseInsensitive(values.asList())

@JvmName("isInArrayCaseInsensitive")
fun isInCaseInsensitive(values: Array<out String>): IsInCaseInsensitive = SqlBuilder.isInCaseInsensitive(values.asList())

fun isInCaseInsensitive(values: Collection<String>): IsInCaseInsensitive = SqlBuilder.isInCaseInsensitive(values)

fun isInCaseInsensitiveWhenPresent(vararg values: String?): IsInCaseInsensitiveWhenPresent =
    isInCaseInsensitiveWhenPresent(values.asList())

@JvmName("isInArrayCaseInsensitiveWhenPresent")
fun isInCaseInsensitiveWhenPresent(values: Array<out String?>?): IsInCaseInsensitiveWhenPresent =
    SqlBuilder.isInCaseInsensitiveWhenPresent(values?.asList())

fun isInCaseInsensitiveWhenPresent(values: Collection<String?>?): IsInCaseInsensitiveWhenPresent =
    SqlBuilder.isInCaseInsensitiveWhenPresent(values)

fun isNotInCaseInsensitive(vararg values: String): IsNotInCaseInsensitive = isNotInCaseInsensitive(values.asList())

@JvmName("isNotInArrayCaseInsensitive")
fun isNotInCaseInsensitive(values: Array<out String>): IsNotInCaseInsensitive =
    SqlBuilder.isNotInCaseInsensitive(values.asList())

fun isNotInCaseInsensitive(values: Collection<String>): IsNotInCaseInsensitive =
    SqlBuilder.isNotInCaseInsensitive(values)

fun isNotInCaseInsensitiveWhenPresent(vararg values: String?): IsNotInCaseInsensitiveWhenPresent =
    isNotInCaseInsensitiveWhenPresent(values.asList())

@JvmName("isNotInArrayCaseInsensitiveWhenPresent")
fun isNotInCaseInsensitiveWhenPresent(values: Array<out String?>?): IsNotInCaseInsensitiveWhenPresent =
    SqlBuilder.isNotInCaseInsensitiveWhenPresent(values?.asList())

fun isNotInCaseInsensitiveWhenPresent(values: Collection<String?>?): IsNotInCaseInsensitiveWhenPresent =
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
class BetweenBuilder<T>(private val value1: T) {
    fun and(value2: T): IsBetween<T> = SqlBuilder.isBetween(value1).and(value2)
}

class BetweenWhenPresentBuilder<T>(private val value1: T?) {
    fun and(value2: T?): IsBetween<T> {
        return SqlBuilder.isBetweenWhenPresent<T>(value1).and(value2)
    }
}

class NotBetweenBuilder<T>(private val value1: T) {
    fun and(value2: T): IsNotBetween<T> = SqlBuilder.isNotBetween(value1).and(value2)
}

class NotBetweenWhenPresentBuilder<T>(private val value1: T?) {
    fun and(value2: T?): IsNotBetween<T> {
        return SqlBuilder.isNotBetweenWhenPresent<T>(value1).and(value2)
    }
}
