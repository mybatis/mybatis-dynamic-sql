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
package org.mybatis.dynamic.sql.util.kotlin.elements

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.render.RenderingStrategy
import java.sql.JDBCType
import kotlin.reflect.KClass

/**
 * This function replaces the native functions in [@see SqlColumn] such as
 * [@see SqlColumn#withTypeHandler], [@see SqlColumn#withRenderingStrategy], etc.
 * This function preserves the non-nullable column type which is lost with the Java
 * native versions.
 */
@SuppressWarnings("LongParameterList")
fun <T : Any> SqlTable.column(
    name: String,
    jdbcType: JDBCType? = null,
    typeHandler: String? = null,
    renderingStrategy: RenderingStrategy? = null,
    parameterTypeConverter: ((T?) -> Any?) = { it },
    javaType: KClass<T>? = null
): SqlColumn<T> = SqlColumn.Builder<T>().run {
    withTable(this@column)
    withName(name)
    withJdbcType(jdbcType)
    withTypeHandler(typeHandler)
    withRenderingStrategy(renderingStrategy)
    withParameterTypeConverter(parameterTypeConverter)
    withJavaType(javaType?.java)
    build()
}
