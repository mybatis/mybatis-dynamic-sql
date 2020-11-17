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

import org.mybatis.dynamic.sql.where.condition.IsEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsInWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsLessThanWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWithSubselect
import org.mybatis.dynamic.sql.where.condition.IsNotInWithSubselect

fun <T> isEqualTo(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsEqualToWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))

fun <T> isNotEqualTo(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsNotEqualToWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))

fun <T> isIn(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsInWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))

fun <T> isNotIn(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsNotInWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))

fun <T> isGreaterThan(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsGreaterThanWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))

fun <T> isGreaterThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsGreaterThanOrEqualToWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))

fun <T> isLessThan(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsLessThanWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))

fun <T> isLessThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> KotlinSubQueryBuilder) =
    IsLessThanOrEqualToWithSubselect.of<T>(subQuery(KotlinSubQueryBuilder()))
