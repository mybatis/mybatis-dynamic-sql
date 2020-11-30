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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable

typealias UpdateCompleter = KotlinUpdateBuilder.() -> Unit

class KotlinUpdateBuilder(private val dsl: UpdateDSL<UpdateModel>) :
    KotlinBaseBuilder<UpdateDSL<UpdateModel>, KotlinUpdateBuilder>(), Buildable<UpdateModel> {

    fun <T> set(column: SqlColumn<T>) = KotlinSetClauseFinisher(column)

    override fun build(): UpdateModel = dsl.build()

    override fun getDsl() = dsl

    override fun self() = this

    @MyBatisDslMarker
    inner class KotlinSetClauseFinisher<T>(private val column: SqlColumn<T>) {
        fun equalToNull() =
            applyToDsl {
                set(column).equalToNull()
            }

        fun equalToConstant(constant: String) =
            applyToDsl {
                set(column).equalToConstant(constant)
            }

        fun equalToStringConstant(constant: String) =
            applyToDsl {
                set(column).equalToStringConstant(constant)
            }

        fun equalTo(value: T) =
            applyToDsl {
                set(column).equalTo(value)
            }

        fun equalTo(value: () -> T) =
            applyToDsl {
                set(column).equalTo(value)
            }

        fun equalTo(rightColumn: BasicColumn) =
            applyToDsl {
                set(column).equalTo(rightColumn)
            }

        fun equalToQueryResult(subQuery: KotlinSubQueryBuilder.() -> Unit) =
            applyToDsl {
                set(column).equalTo(KotlinSubQueryBuilder().apply(subQuery))
            }

        fun equalToWhenPresent(value: () -> T?) =
            applyToDsl {
                set(column).equalToWhenPresent(value)
            }

        fun equalToWhenPresent(value: T?) =
            applyToDsl {
                set(column).equalToWhenPresent(value)
            }

        private fun applyToDsl(block: UpdateDSL<UpdateModel>.() -> Unit) =
            this@KotlinUpdateBuilder.apply {
                dsl.apply(block)
            }
    }
}
