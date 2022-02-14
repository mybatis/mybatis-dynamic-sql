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

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.util.Buildable

typealias GeneralInsertCompleter = @MyBatisDslMarker KotlinGeneralInsertBuilder.() -> Unit

@MyBatisDslMarker
class KotlinGeneralInsertBuilder(private val dsl: GeneralInsertDSL) : Buildable<GeneralInsertModel> {

    fun <T> set(column: SqlColumn<T>): GeneralInsertSetClauseFinisher<T> = GeneralInsertSetClauseFinisher(column)

    override fun build(): GeneralInsertModel = dsl.build()

    @MyBatisDslMarker
    inner class GeneralInsertSetClauseFinisher<T>(private val column: SqlColumn<T>) {
        fun toNull(): Unit =
            applyToDsl {
                set(column).toNull()
            }

        infix fun toConstant(constant: String): Unit =
            applyToDsl {
                set(column).toConstant(constant)
            }

        infix fun toStringConstant(constant: String): Unit =
            applyToDsl {
                set(column).toStringConstant(constant)
            }

        infix fun toValue(value: T): Unit = toValue { value }

        infix fun toValue(value: () -> T): Unit =
            applyToDsl {
                set(column).toValue(value)
            }

        infix fun toValueOrNull(value: T?): Unit = toValueOrNull { value }

        infix fun toValueOrNull(value: () -> T?): Unit =
            applyToDsl {
                set(column).toValueOrNull(value)
            }

        infix fun toValueWhenPresent(value: T?): Unit = toValueWhenPresent { value }

        infix fun toValueWhenPresent(value: () -> T?): Unit =
            applyToDsl {
                set(column).toValueWhenPresent(value)
            }

        private fun applyToDsl(block: GeneralInsertDSL.() -> Unit) {
            this@KotlinGeneralInsertBuilder.dsl.apply(block)
        }
    }
}
