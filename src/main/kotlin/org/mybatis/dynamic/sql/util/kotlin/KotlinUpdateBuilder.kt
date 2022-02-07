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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable

typealias UpdateCompleter = KotlinUpdateBuilder.() -> Unit

class KotlinUpdateBuilder(private val dsl: UpdateDSL<UpdateModel>) :
    KotlinBaseBuilder<UpdateDSL<UpdateModel>>(), Buildable<UpdateModel> {

    fun <T> set(column: SqlColumn<T>): KotlinSetClauseFinisher<T> = KotlinSetClauseFinisher(column)

    override fun build(): UpdateModel = dsl.build()

    override fun getDsl(): UpdateDSL<UpdateModel> = dsl

    @MyBatisDslMarker
    @Suppress("TooManyFunctions")
    inner class KotlinSetClauseFinisher<T>(private val column: SqlColumn<T>) {
        fun equalToNull(): Unit =
            applyToDsl {
                set(column).equalToNull()
            }

        fun equalToConstant(constant: String): Unit =
            applyToDsl {
                set(column).equalToConstant(constant)
            }

        fun equalToStringConstant(constant: String): Unit =
            applyToDsl {
                set(column).equalToStringConstant(constant)
            }

        fun equalTo(value: T): Unit = equalTo { value }

        fun equalTo(value: () -> T): Unit =
            applyToDsl {
                set(column).equalTo(value)
            }

        fun equalTo(rightColumn: BasicColumn): Unit =
            applyToDsl {
                set(column).equalTo(rightColumn)
            }

        fun equalToOrNull(value: T?): Unit = equalToOrNull { value }

        fun equalToOrNull(value: () -> T?): Unit =
            applyToDsl {
                set(column).equalToOrNull(value)
            }

        fun equalToQueryResult(subQuery: KotlinSubQueryBuilder.() -> Unit): Unit =
            applyToDsl {
                set(column).equalTo(KotlinSubQueryBuilder().apply(subQuery))
            }

        fun equalToWhenPresent(value: () -> T?): Unit =
            applyToDsl {
                set(column).equalToWhenPresent(value)
            }

        fun equalToWhenPresent(value: T?): Unit = equalToWhenPresent { value }

        private fun applyToDsl(block: UpdateDSL<UpdateModel>.() -> Unit) {
            this@KotlinUpdateBuilder.dsl.apply(block)
        }
    }
}
