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

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.CountDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

typealias CountCompleter = KotlinCountBuilder.() -> Unit

class KotlinCountBuilder(private val fromGatherer: CountDSL.FromGatherer<SelectModel>) :
    KotlinBaseJoiningBuilder<CountDSL<SelectModel>, KotlinCountBuilder>(),
    Buildable<SelectModel> {

    private lateinit var dsl: CountDSL<SelectModel>

    fun from(table: SqlTable): KotlinCountBuilder =
        apply {
            dsl = fromGatherer.from(table)
        }

    override fun build(): SelectModel = getDsl().build()

    override fun self(): KotlinCountBuilder = this

    override fun getDsl(): CountDSL<SelectModel> {
        try {
            return dsl
        } catch (e: UninitializedPropertyAccessException) {
            throw UninitializedPropertyAccessException(
                "You must specify a \"from\" clause before any other clauses in a count statement", e
            )
        }
    }
}
