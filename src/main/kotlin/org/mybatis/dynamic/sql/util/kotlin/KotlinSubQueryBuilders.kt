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
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn

@MyBatisDslMarker
sealed class KotlinBaseSubQueryBuilder<T : KotlinBaseSubQueryBuilder<T> > {
    lateinit var selectBuilder: KotlinSelectBuilder

    fun select(vararg selectList: BasicColumn, completer: SelectCompleter) =
        select(selectList.toList(), completer)

    fun select(selectList: List<BasicColumn>, completer: SelectCompleter) =
        applySelf {
            selectBuilder = completer(KotlinSelectBuilder(SqlBuilder.select(selectList)))
        }

    fun selectDistinct(vararg selectList: BasicColumn, completer: SelectCompleter) =
        selectDistinct(selectList.toList(), completer)

    fun selectDistinct(selectList: List<BasicColumn>, completer: SelectCompleter) =
        applySelf {
            selectBuilder = completer(KotlinSelectBuilder(SqlBuilder.selectDistinct(selectList)))
        }

    private fun applySelf(block: T.() -> Unit): T =
        self().apply { block() }

    protected abstract fun self(): T
}

class KotlinSubQueryBuilder: KotlinBaseSubQueryBuilder<KotlinSubQueryBuilder>() {
    override fun self() = this
}

class KotlinQualifiedSubQueryBuilder: KotlinBaseSubQueryBuilder<KotlinQualifiedSubQueryBuilder>() {
    var correlationName: String? = null

    operator fun String.unaryPlus(): KotlinQualifiedSubQueryBuilder {
        correlationName = this
        return self()
    }

    override fun self() = this
}

class KotlinInsertSelectSubQueryBuilder : KotlinBaseSubQueryBuilder<KotlinInsertSelectSubQueryBuilder>() {
    lateinit var columnList : List<SqlColumn<*>>

    fun columns(vararg columnList : SqlColumn<*>) =
        columns(columnList.asList())

    fun columns(columnList : List<SqlColumn<*>>) =
        apply {
            this.columnList = columnList
        }

    override fun self() = this
}
