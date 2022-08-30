/*
 *    Copyright 2016-2022 the original author or authors.
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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.InsertSelectModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.Messages

@MyBatisDslMarker
sealed class KotlinBaseSubQueryBuilder {
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

    internal fun buildSelectModel(): SelectModel =
        selectBuilder?.build()?: throw KInvalidSQLException(Messages.getString("ERROR.28")) //$NON-NLS-1$
}

class KotlinSubQueryBuilder : KotlinBaseSubQueryBuilder(), Buildable<SelectModel> {
    override fun build(): SelectModel = buildSelectModel()
}

class KotlinQualifiedSubQueryBuilder : KotlinBaseSubQueryBuilder(), Buildable<SelectModel> {
    var correlationName: String? = null

    operator fun String.unaryPlus() {
        correlationName = this
    }

    override fun build(): SelectModel = buildSelectModel()
}

typealias InsertSelectCompleter = KotlinInsertSelectSubQueryBuilder.() -> Unit

class KotlinInsertSelectSubQueryBuilder : KotlinBaseSubQueryBuilder(), Buildable<InsertSelectModel> {
    private var columnList: List<SqlColumn<*>>? = null
    private var table: SqlTable? = null

    fun into(table: SqlTable) {
        this.table = table
    }

    fun columns(vararg columnList: SqlColumn<*>): Unit = columns(columnList.asList())

    fun columns(columnList: List<SqlColumn<*>>) {
        this.columnList = columnList
    }

    override fun build(): InsertSelectModel {
        if (table == null) {
            throw KInvalidSQLException(Messages.getString("ERROR.29")) //$NON-NLS-1$
        }

        return if (columnList == null) {
            SqlBuilder.insertInto(table)
                .withSelectStatement { buildSelectModel() }
                .build()
        } else {
            SqlBuilder.insertInto(table)
                .withColumnList(columnList)
                .withSelectStatement { buildSelectModel() }
                .build()
        }
    }
}
