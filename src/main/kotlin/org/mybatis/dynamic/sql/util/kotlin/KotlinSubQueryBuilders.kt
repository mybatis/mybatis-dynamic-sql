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
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

@MyBatisDslMarker
sealed class KotlinBaseSubQueryBuilder<T : KotlinBaseSubQueryBuilder<T>> : Buildable<SelectModel> {
    private lateinit var selectBuilder: KotlinSelectBuilder

    fun select(vararg selectList: BasicColumn, completer: SelectCompleter): T =
        select(selectList.toList(), completer)

    fun select(selectList: List<BasicColumn>, completer: SelectCompleter): T =
        applySelf {
            selectBuilder = KotlinSelectBuilder(SqlBuilder.select(selectList)).apply(completer)
        }

    fun selectDistinct(vararg selectList: BasicColumn, completer: SelectCompleter): T =
        selectDistinct(selectList.toList(), completer)

    fun selectDistinct(selectList: List<BasicColumn>, completer: SelectCompleter): T =
        applySelf {
            selectBuilder = KotlinSelectBuilder(SqlBuilder.selectDistinct(selectList)).apply(completer)
        }

    override fun build(): SelectModel =
        try {
            selectBuilder.build()
        } catch (e: UninitializedPropertyAccessException) {
            throw UninitializedPropertyAccessException(
                "You must specify a select statement", e
            )
        }

    private fun applySelf(block: T.() -> Unit): T =
        self().apply { block() }

    protected abstract fun self(): T
}

class KotlinSubQueryBuilder : KotlinBaseSubQueryBuilder<KotlinSubQueryBuilder>() {
    override fun self(): KotlinSubQueryBuilder = this
}

class KotlinQualifiedSubQueryBuilder : KotlinBaseSubQueryBuilder<KotlinQualifiedSubQueryBuilder>() {
    var correlationName: String? = null

    operator fun String.unaryPlus(): KotlinQualifiedSubQueryBuilder {
        correlationName = this
        return self()
    }

    override fun self(): KotlinQualifiedSubQueryBuilder = this
}

class KotlinInsertSelectSubQueryBuilder : KotlinBaseSubQueryBuilder<KotlinInsertSelectSubQueryBuilder>() {
    private lateinit var lateColumnList: List<SqlColumn<*>>

    val columnList: List<SqlColumn<*>>
        get(): List<SqlColumn<*>> =
            try {
                lateColumnList
            } catch (e: UninitializedPropertyAccessException) {
                throw UninitializedPropertyAccessException(
                    "You must specify a column list in an insert with select statement", e
                )
            }

    fun columns(vararg columnList: SqlColumn<*>): KotlinInsertSelectSubQueryBuilder =
        columns(columnList.asList())

    fun columns(columnList: List<SqlColumn<*>>): KotlinInsertSelectSubQueryBuilder =
        apply {
            this.lateColumnList = columnList
        }

    override fun self(): KotlinInsertSelectSubQueryBuilder = this
}
