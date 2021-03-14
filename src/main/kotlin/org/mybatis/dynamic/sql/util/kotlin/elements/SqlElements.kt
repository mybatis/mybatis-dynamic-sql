/*
 *    Copyright 2016-2021 the original author or authors.
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
@file:Suppress("TooManyFunctions")
package org.mybatis.dynamic.sql.util.kotlin.elements

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.Constant
import org.mybatis.dynamic.sql.ExistsPredicate
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.StringConstant
import org.mybatis.dynamic.sql.select.ColumnSortSpecification
import org.mybatis.dynamic.sql.select.SimpleSortSpecification
import org.mybatis.dynamic.sql.select.aggregate.Avg
import org.mybatis.dynamic.sql.select.aggregate.Count
import org.mybatis.dynamic.sql.select.aggregate.CountAll
import org.mybatis.dynamic.sql.select.aggregate.CountDistinct
import org.mybatis.dynamic.sql.select.aggregate.Max
import org.mybatis.dynamic.sql.select.aggregate.Min
import org.mybatis.dynamic.sql.select.aggregate.Sum
import org.mybatis.dynamic.sql.select.function.Add
import org.mybatis.dynamic.sql.select.function.Concatenate
import org.mybatis.dynamic.sql.select.function.Divide
import org.mybatis.dynamic.sql.select.function.Lower
import org.mybatis.dynamic.sql.select.function.Multiply
import org.mybatis.dynamic.sql.select.function.OperatorFunction
import org.mybatis.dynamic.sql.select.function.Substring
import org.mybatis.dynamic.sql.select.function.Subtract
import org.mybatis.dynamic.sql.select.function.Upper
import org.mybatis.dynamic.sql.select.join.EqualTo
import org.mybatis.dynamic.sql.util.kotlin.KotlinSubQueryBuilder
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

// join support
fun equalTo(column: BasicColumn): EqualTo = EqualTo(column)

// aggregate support
fun count() = CountAll()

fun count(column: BasicColumn): Count = Count.of(column)

fun countDistinct(column: BasicColumn): CountDistinct = CountDistinct.of(column)

fun <T> max(column: BindableColumn<T>): Max<T> = Max.of(column)

fun <T> min(column: BindableColumn<T>): Min<T> = Min.of(column)

fun <T> avg(column: BindableColumn<T>): Avg<T> = Avg.of(column)

fun <T> sum(column: BindableColumn<T>): Sum<T> = Sum.of(column)

// constants
fun <T> constant(constant: String): Constant<T> = Constant.of(constant)

fun stringConstant(constant: String): StringConstant = StringConstant.of(constant)

// functions
fun <T : Number> add(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Add<T> = Add.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Number> divide(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Divide<T> = Divide.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Number> multiply(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Multiply<T> = Multiply.of(firstColumn, secondColumn, subsequentColumns.asList())

fun <T : Number> subtract(
    firstColumn: BindableColumn<T>,
    secondColumn: BasicColumn,
    vararg subsequentColumns: BasicColumn
): Subtract<T> = Subtract.of(firstColumn, secondColumn, subsequentColumns.asList())

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

fun lower(column: BindableColumn<String>): Lower = Lower.of(column)

fun substring(
    column: BindableColumn<String>,
    offset: Int,
    length: Int
): Substring = Substring.of(column, offset, length)

fun upper(column: BindableColumn<String>): Upper = Upper.of(column)

// conditions for all data types
fun <T> isNull(): IsNull<T> = IsNull<T>()

fun <T> isNotNull(): IsNotNull<T> = IsNotNull<T>()

fun exists(subQuery: KotlinSubQueryBuilder.() -> Unit): ExistsPredicate =
    ExistsPredicate.exists(KotlinSubQueryBuilder().apply(subQuery))

fun notExists(subQuery: KotlinSubQueryBuilder.() -> Unit): ExistsPredicate =
    ExistsPredicate.notExists(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isEqualTo(value: T): IsEqualTo<T> = isEqualToProperty { value }

fun <T> isEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsEqualToWithSubselect<T> =
    IsEqualToWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isEqualTo(column: BasicColumn): IsEqualToColumn<T> =
    IsEqualToColumn.of(column)

fun <T : Any> isEqualToProperty(supplier: () -> T): IsEqualTo<T> =
    IsEqualTo.of(supplier)

fun <T : Any> isEqualToWhenPresent(value: T?): IsEqualToWhenPresent<T> = isEqualToWhenPresent { value }

fun <T : Any> isEqualToWhenPresent(valueSupplier: () -> T?): IsEqualToWhenPresent<T> =
    IsEqualToWhenPresent.of(valueSupplier)

fun <T : Any> isNotEqualTo(value: T): IsNotEqualTo<T> = isNotEqualToProperty { value }

fun <T> isNotEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotEqualToWithSubselect<T> =
    IsNotEqualToWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isNotEqualTo(column: BasicColumn): IsNotEqualToColumn<T> =
    IsNotEqualToColumn.of(column)

fun <T : Any> isNotEqualToProperty(supplier: () -> T): IsNotEqualTo<T> =
    IsNotEqualTo.of(supplier)

fun <T : Any> isNotEqualToWhenPresent(value: T?): IsNotEqualToWhenPresent<T> = isNotEqualToWhenPresent { value }

fun <T : Any> isNotEqualToWhenPresent(valueSupplier: () -> T?): IsNotEqualToWhenPresent<T> =
    IsNotEqualToWhenPresent.of(valueSupplier)

fun <T : Any> isGreaterThan(value: T): IsGreaterThan<T> = isGreaterThanProperty { value }

fun <T> isGreaterThan(subQuery: KotlinSubQueryBuilder.() -> Unit) : IsGreaterThanWithSubselect<T> =
    IsGreaterThanWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isGreaterThan(column: BasicColumn): IsGreaterThanColumn<T> =
    IsGreaterThanColumn.of(column)

fun <T : Any> isGreaterThanProperty(supplier: () -> T): IsGreaterThan<T> =
    IsGreaterThan.of(supplier)

fun <T : Any> isGreaterThanWhenPresent(value: T?): IsGreaterThanWhenPresent<T> = isGreaterThanWhenPresent { value }

fun <T : Any> isGreaterThanWhenPresent(valueSupplier: () -> T?): IsGreaterThanWhenPresent<T> =
    IsGreaterThanWhenPresent.of(valueSupplier)

fun <T : Any> isGreaterThanOrEqualTo(value: T): IsGreaterThanOrEqualTo<T> = isGreaterThanOrEqualToProperty { value }

fun <T> isGreaterThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsGreaterThanOrEqualToWithSubselect<T> =
    IsGreaterThanOrEqualToWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isGreaterThanOrEqualTo(column: BasicColumn): IsGreaterThanOrEqualToColumn<T> =
    IsGreaterThanOrEqualToColumn.of(column)

fun <T : Any> isGreaterThanOrEqualToProperty(supplier: () -> T): IsGreaterThanOrEqualTo<T> =
    IsGreaterThanOrEqualTo.of(supplier)

fun <T : Any> isGreaterThanOrEqualToWhenPresent(value: T?): IsGreaterThanOrEqualToWhenPresent<T> =
    isGreaterThanOrEqualToWhenPresent { value }

fun <T : Any> isGreaterThanOrEqualToWhenPresent(valueSupplier: () -> T?): IsGreaterThanOrEqualToWhenPresent<T> =
    IsGreaterThanOrEqualToWhenPresent.of(valueSupplier)

fun <T : Any> isLessThan(value: T): IsLessThan<T> = isLessThanProperty { value }

fun <T> isLessThan(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanWithSubselect<T> =
    IsLessThanWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isLessThan(column: BasicColumn): IsLessThanColumn<T> =
    IsLessThanColumn.of(column)

fun <T : Any> isLessThanProperty(supplier: () -> T): IsLessThan<T> =
    IsLessThan.of(supplier)

fun <T : Any> isLessThanWhenPresent(value: T?): IsLessThanWhenPresent<T> = isLessThanWhenPresent { value }

fun <T : Any> isLessThanWhenPresent(valueSupplier: () -> T?): IsLessThanWhenPresent<T> =
    IsLessThanWhenPresent.of(valueSupplier)

fun <T : Any> isLessThanOrEqualTo(value: T): IsLessThanOrEqualTo<T> = isLessThanOrEqualToProperty { value }

fun <T> isLessThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanOrEqualToWithSubselect<T> =
    IsLessThanOrEqualToWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T> isLessThanOrEqualTo(column: BasicColumn): IsLessThanOrEqualToColumn<T> =
    IsLessThanOrEqualToColumn.of(column)

fun <T : Any> isLessThanOrEqualToProperty(supplier: () -> T): IsLessThanOrEqualTo<T> =
    IsLessThanOrEqualTo.of(supplier)

fun <T : Any> isLessThanOrEqualToWhenPresent(value: T?): IsLessThanOrEqualToWhenPresent<T> =
    isLessThanOrEqualToWhenPresent { value }

fun <T : Any> isLessThanOrEqualToWhenPresent(valueSupplier: () -> T?): IsLessThanOrEqualToWhenPresent<T> =
    IsLessThanOrEqualToWhenPresent.of(valueSupplier)

fun <T : Any> isIn(vararg values: T): IsIn<T> = isIn(values.asList())

fun <T : Any> isIn(values: Collection<T>): IsIn<T> = IsIn.of(values)

fun <T> isIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsInWithSubselect<T> =
    IsInWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isInWhenPresent(vararg values: T?): IsInWhenPresent<T> = isInWhenPresent(values.asList())

fun <T : Any> isInWhenPresent(values: Collection<T?>): IsInWhenPresent<T> =
    IsInWhenPresent.of(values)

fun <T : Any> isNotIn(vararg values: T): IsNotIn<T> = isNotIn(values.asList())

fun <T : Any> isNotIn(values: Collection<T>): IsNotIn<T> = IsNotIn.of(values)

fun <T> isNotIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotInWithSubselect<T> =
    IsNotInWithSubselect.of(KotlinSubQueryBuilder().apply(subQuery))

fun <T : Any> isNotInWhenPresent(vararg values: T?): IsNotInWhenPresent<T> = isNotInWhenPresent(values.asList())

fun <T : Any> isNotInWhenPresent(values: Collection<T?>): IsNotInWhenPresent<T> = IsNotInWhenPresent.of(values)

fun <T : Any> isBetween(value1: T): BetweenBuilder<T> = isBetween { value1 }

fun <T : Any> isBetween(valueSupplier1: () -> T): BetweenBuilder<T> = BetweenBuilder(valueSupplier1)

fun <T : Any> isBetweenWhenPresent(value1: T?): BetweenWhenPresentBuilder<T> = isBetweenWhenPresent { value1 }

fun <T : Any> isBetweenWhenPresent(valueSupplier1: () -> T?): BetweenWhenPresentBuilder<T> =
    BetweenWhenPresentBuilder(valueSupplier1)

fun <T : Any> isNotBetween(value1: T): NotBetweenBuilder<T> = isNotBetween { value1 }

fun <T : Any> isNotBetween(valueSupplier1: () -> T): NotBetweenBuilder<T> = NotBetweenBuilder(valueSupplier1)

fun <T : Any> isNotBetweenWhenPresent(value1: T?): NotBetweenWhenPresentBuilder<T> = isNotBetweenWhenPresent { value1 }

fun <T : Any> isNotBetweenWhenPresent(valueSupplier1: () -> T?): NotBetweenWhenPresentBuilder<T> =
    NotBetweenWhenPresentBuilder(valueSupplier1)

// for string columns, but generic for columns with type handlers
fun <T : Any> isLike(value: T): IsLike<T> = isLike { value }

fun <T : Any> isLike(valueSupplier: () -> T): IsLike<T> = IsLike.of(valueSupplier)

fun <T : Any> isLikeWhenPresent(value: T?): IsLikeWhenPresent<T> = isLikeWhenPresent { value }

fun <T : Any> isLikeWhenPresent(valueSupplier: () -> T?): IsLikeWhenPresent<T> =
    IsLikeWhenPresent.of(valueSupplier)

fun <T : Any> isNotLike(value: T): IsNotLike<T> = isNotLike { value }

fun <T : Any> isNotLike(valueSupplier: () -> T): IsNotLike<T> = IsNotLike.of(valueSupplier)

fun <T : Any> isNotLikeWhenPresent(value: T?): IsNotLikeWhenPresent<T> = isNotLikeWhenPresent { value }

fun <T : Any> isNotLikeWhenPresent(valueSupplier: () -> T?): IsNotLikeWhenPresent<T> =
    IsNotLikeWhenPresent.of(valueSupplier)

// shortcuts for booleans
fun isTrue(): IsEqualTo<Boolean> = isEqualTo(true)

fun isFalse(): IsEqualTo<Boolean> = isEqualTo(false)

// conditions for strings only
fun isLikeCaseInsensitive(value: String): IsLikeCaseInsensitive = isLikeCaseInsensitive { value }

fun isLikeCaseInsensitive(valueSupplier: () -> String): IsLikeCaseInsensitive =
    IsLikeCaseInsensitive.of(valueSupplier)

fun isLikeCaseInsensitiveWhenPresent(value: String?): IsLikeCaseInsensitiveWhenPresent =
    isLikeCaseInsensitiveWhenPresent { value }

fun isLikeCaseInsensitiveWhenPresent(valueSupplier: () -> String?): IsLikeCaseInsensitiveWhenPresent =
    IsLikeCaseInsensitiveWhenPresent.of(valueSupplier)

fun isNotLikeCaseInsensitive(value: String): IsNotLikeCaseInsensitive = isNotLikeCaseInsensitive { value }

fun isNotLikeCaseInsensitive(valueSupplier: () -> String): IsNotLikeCaseInsensitive =
    IsNotLikeCaseInsensitive.of(valueSupplier)

fun isNotLikeCaseInsensitiveWhenPresent(value: String?): IsNotLikeCaseInsensitiveWhenPresent =
    isNotLikeCaseInsensitiveWhenPresent { value }

fun isNotLikeCaseInsensitiveWhenPresent(valueSupplier: () -> String?): IsNotLikeCaseInsensitiveWhenPresent =
    IsNotLikeCaseInsensitiveWhenPresent.of(valueSupplier)

fun isInCaseInsensitive(vararg values: String): IsInCaseInsensitive = isInCaseInsensitive(values.asList())

fun isInCaseInsensitive(values: Collection<String>): IsInCaseInsensitive = IsInCaseInsensitive.of(values)

fun isInCaseInsensitiveWhenPresent(vararg values: String?): IsInCaseInsensitiveWhenPresent =
    isInCaseInsensitiveWhenPresent(values.asList())

fun isInCaseInsensitiveWhenPresent(values: Collection<String?>): IsInCaseInsensitiveWhenPresent =
    IsInCaseInsensitiveWhenPresent.of(values)

fun isNotInCaseInsensitive(vararg values: String): IsNotInCaseInsensitive = isNotInCaseInsensitive(values.asList())

fun isNotInCaseInsensitive(values: Collection<String>): IsNotInCaseInsensitive = IsNotInCaseInsensitive.of(values)

fun isNotInCaseInsensitiveWhenPresent(vararg values: String?): IsNotInCaseInsensitiveWhenPresent =
    isNotInCaseInsensitiveWhenPresent(values.asList())

fun isNotInCaseInsensitiveWhenPresent(values: Collection<String?>): IsNotInCaseInsensitiveWhenPresent =
    IsNotInCaseInsensitiveWhenPresent.of(values)

// order by support
/**
 * Creates a sort specification based on a String. This is useful when a column has been
 * aliased in the select list. For example:
 *
 * <pre>
 * select(foo.as("bar"))
 * .from(baz)
 * .orderBy(sortColumn("bar"))
 *  </pre>
 *
 *
 * @param name the string to use as a sort specification
 * @return a sort specification
 */
fun sortColumn(name: String): SortSpecification = SimpleSortSpecification.of(name)

/**
 * Creates a sort specification based on a column and a table alias. This can be useful in a join
 * where the desired sort order is based on a column not in the select list. This will likely
 * fail in union queries depending on database support.
 *
 * @param tableAlias the table alias
 * @param column the column
 * @return a sort specification
 */
fun sortColumn(tableAlias: String, column: SqlColumn<*>): SortSpecification =
    ColumnSortSpecification(tableAlias, column)

// DSL Support Classes
class BetweenBuilder<T>(private val valueSupplier1: () -> T) {
    fun and(value2: T): IsBetween<T> {
        return and { value2 }
    }

    fun and(valueSupplier2: () -> T): IsBetween<T> {
        return IsBetween.isBetween(valueSupplier1).and(valueSupplier2)
    }
}

class BetweenWhenPresentBuilder<T>(private val valueSupplier1: () -> T?) {
    fun and(value2: T?): IsBetweenWhenPresent<T> {
        return and { value2 }
    }

    fun and(valueSupplier2: () -> T?): IsBetweenWhenPresent<T> {
        return IsBetweenWhenPresent.isBetweenWhenPresent<T>(valueSupplier1).and(valueSupplier2)
    }
}

class NotBetweenBuilder<T>(private val valueSupplier1: () -> T) {
    fun and(value2: T): IsNotBetween<T> {
        return and { value2 }
    }

    fun and(valueSupplier2: () -> T): IsNotBetween<T> {
        return IsNotBetween.isNotBetween(valueSupplier1).and(valueSupplier2)
    }
}

class NotBetweenWhenPresentBuilder<T>(private val valueSupplier1: () -> T?) {
    fun and(value2: T?): IsNotBetweenWhenPresent<T> {
        return and { value2 }
    }

    fun and(valueSupplier2: () -> T?): IsNotBetweenWhenPresent<T> {
        return IsNotBetweenWhenPresent.isNotBetweenWhenPresent<T>(valueSupplier1).and(valueSupplier2)
    }
}
