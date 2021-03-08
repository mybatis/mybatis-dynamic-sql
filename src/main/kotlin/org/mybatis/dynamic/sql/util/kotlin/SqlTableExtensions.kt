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
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.ParameterTypeConverter
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.render.RenderingStrategy
import java.sql.JDBCType

/**
 * These functions replace the native functions in [@see SqlColumn} such as
 * [@see SqlColumn#withTypeHandler], [@see SqlColumn#withRenderingStrategy], etc.
 * These functions preserve the non-nullable column type which is lost with the Java
 * native versions.
 */

fun <T : Any> SqlTable.column(name: String, modifier: SqlColumnModifier<T>.() -> Unit): SqlColumn<T> =
    with(SqlColumnModifier(column<T>(name))) {
        modifier(this)
        column
    }

fun <T : Any> SqlTable.column(
    name: String,
    jdbcType: JDBCType,
    modifier: SqlColumnModifier<T>.() -> Unit
): SqlColumn<T> =
    with(SqlColumnModifier(column<T>(name, jdbcType))) {
        modifier(this)
        column
    }

@MyBatisDslMarker
class SqlColumnModifier<T>(var column: SqlColumn<T>) {
    fun withTypeHandler(typeHandler: String) {
        column = column.withTypeHandler(typeHandler)
    }

    fun withRenderingStrategy(renderingStrategy: RenderingStrategy) {
        column = column.withRenderingStrategy(renderingStrategy)
    }

    fun withParameterTypeConverter(parameterTypeConverter: ParameterTypeConverter<T, *>) {
        column = column.withParameterTypeConverter(parameterTypeConverter)
    }
}
