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

import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel
import org.mybatis.dynamic.sql.util.Buildable

typealias KotlinMultiRowInsertCompleter<T> = KotlinMultiRowInsertBuilder<T>.() -> Unit

@MyBatisDslMarker
class KotlinMultiRowInsertBuilder<T> (private val rows: Collection<T>): Buildable<MultiRowInsertModel<T>> {

    private lateinit var dsl: MultiRowInsertDSL<T>

    fun into(table: SqlTable) {
        dsl = SqlBuilder.insertMultiple(rows).into(table)
    }

    infix fun <C : Any> map(column: SqlColumn<C>) = MapCompleter(column)

    override fun build(): MultiRowInsertModel<T> {
        return getDsl().build()
    }

    private fun getDsl(): MultiRowInsertDSL<T> {
        try {
            return dsl
        } catch (e: UninitializedPropertyAccessException) {
            throw UninitializedPropertyAccessException(
                "You must specify an \"into\" clause before any other clauses in an insertMultiple statement", e
            )
        }
    }

    @MyBatisDslMarker
    inner class MapCompleter<C : Any> (private val column: SqlColumn<C>) {
        infix fun toProperty(property: String) =
            applyToDsl {
                map(column).toProperty(property)
            }

        fun toNull() =
            applyToDsl {
                map(column).toNull()
            }

        infix fun toConstant(constant: String) =
            applyToDsl {
                map(column).toConstant(constant)
            }

        infix fun toStringConstant(constant: String) =
            applyToDsl {
                map(column).toStringConstant(constant)
            }

        private fun applyToDsl(block: MultiRowInsertDSL<T>.() -> Unit) {
            this@KotlinMultiRowInsertBuilder.getDsl().apply(block)
        }
    }
}
