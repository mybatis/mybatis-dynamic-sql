/*
 *    Copyright 2016-2024 the original author or authors.
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

import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.delete.DeleteDSL
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.util.Buildable

typealias DeleteCompleter = KotlinDeleteBuilder.() -> Unit

class KotlinDeleteBuilder(private val dsl: DeleteDSL<DeleteModel>) :
    KotlinBaseBuilder<DeleteDSL<DeleteModel>>(), Buildable<DeleteModel> {

    fun orderBy(vararg columns: SortSpecification) {
        dsl.orderBy(columns.toList())
    }

    fun limit(limit: Long) {
        limitWhenPresent(limit)
    }

    fun limitWhenPresent(limit: Long?) {
        dsl.limitWhenPresent(limit)
    }

    override fun build(): DeleteModel = dsl.build()

    override fun getDsl(): DeleteDSL<DeleteModel> = dsl
}
