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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import java.lang.NullPointerException

@MyBatisDslMarker
sealed class KotlinBaseSubQueryBuilder : Buildable<SelectModel> {
    private var selectBuilder: KotlinSelectBuilder? = null

    fun select(vararg selectList: BasicColumn, completer: SelectCompleter): Unit =
        select(selectList.toList(), completer)

    fun select(selectList: List<BasicColumn>, completer: SelectCompleter) {
        selectBuilder = KotlinSelectBuilder(SqlBuilder.select(selectList)).apply(completer)
    }

    fun selectDistinct(vararg selectList: BasicColumn, completer: SelectCompleter): Unit =
        selectDistinct(selectList.toList(), completer)

    fun selectDistinct(selectList: List<BasicColumn>, completer: SelectCompleter) {
        selectBuilder = KotlinSelectBuilder(SqlBuilder.selectDistinct(selectList)).apply(completer)
    }

    override fun build(): SelectModel =
        try {
            selectBuilder!!.build()
        } catch (e: NullPointerException) {
            throw KInvalidSQLException("You must specify a select statement in a sub query")
        }
}

class KotlinSubQueryBuilder : KotlinBaseSubQueryBuilder()

class KotlinQualifiedSubQueryBuilder : KotlinBaseSubQueryBuilder() {
    var correlationName: String? = null

    operator fun String.unaryPlus() {
        correlationName = this
    }
}

typealias InsertSelectCompleter = KotlinInsertSelectSubQueryBuilder.() -> Unit

class KotlinInsertSelectSubQueryBuilder : KotlinBaseSubQueryBuilder() {
    private var columnList: List<SqlColumn<*>>? = null

    internal fun columnList(): List<SqlColumn<*>> =
        columnList?: throw KInvalidSQLException("You must specify a column list in an insert select statement")

    fun columns(vararg columnList: SqlColumn<*>): Unit = columns(columnList.asList())

    fun columns(columnList: List<SqlColumn<*>>) {
        this.columnList = columnList
    }
}
