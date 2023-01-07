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
package org.mybatis.dynamic.sql.util.kotlin.elements

import org.mybatis.dynamic.sql.DerivedColumn
import org.mybatis.dynamic.sql.SqlColumn

infix fun <T> DerivedColumn<T>.`as`(alias: String): DerivedColumn<T> = this.`as`(alias)

infix fun <T> SqlColumn<T>.`as`(alias: String): SqlColumn<T> = this.`as`(alias)

/**
 * Adds a qualifier to a column for use with table aliases (typically in joins or sub queries).
 * This is as close to natural SQL syntax as we can get in Kotlin. Natural SQL would look like
 * "qualifier.column". With this function we can say "qualifier(column)".
 */
operator fun <T> String.invoke(column: SqlColumn<T>): SqlColumn<T> = column.qualifiedWith(this)
