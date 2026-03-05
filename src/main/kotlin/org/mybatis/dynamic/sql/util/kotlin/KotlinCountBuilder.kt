/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.dsl.CountDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

typealias CountCompleter = KotlinCountBuilder.() -> Unit

class KotlinCountBuilder(private val dsl: CountDSL) :
    KotlinJoinOperations<CountDSL>(dsl),
    Buildable<SelectModel> {

    fun from(table: SqlTable): KotlinCountBuilder =
        apply {
            dsl.from(table)
        }

    override fun build(): SelectModel {
        return dsl.build()
    }
}
