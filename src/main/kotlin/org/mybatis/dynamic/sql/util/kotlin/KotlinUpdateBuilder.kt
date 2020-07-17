/**
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

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable

// insert completers are here because sonar doesn't see them as covered if they are in a file by themselves
typealias GeneralInsertCompleter = GeneralInsertDSL.() -> GeneralInsertDSL

typealias InsertCompleter<T> = InsertDSL<T>.() -> InsertDSL<T>

typealias MultiRowInsertCompleter<T> = MultiRowInsertDSL<T>.() -> MultiRowInsertDSL<T>

typealias UpdateCompleter = KotlinUpdateBuilder.() -> Buildable<UpdateModel>

class KotlinUpdateBuilder(private val dsl: UpdateDSL<UpdateModel>) :
    KotlinBaseBuilder<UpdateModel, UpdateDSL<UpdateModel>.UpdateWhereBuilder, KotlinUpdateBuilder>() {

    fun <T> set(column: SqlColumn<T>): UpdateDSL<UpdateModel>.SetClauseFinisher<T> = dsl.set(column)

    override fun build(): UpdateModel = dsl.build()

    override fun getWhere(): UpdateDSL<UpdateModel>.UpdateWhereBuilder = dsl.where()

    override fun self() = this
}
