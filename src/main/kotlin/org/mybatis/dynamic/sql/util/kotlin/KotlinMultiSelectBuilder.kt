/*
 *    Copyright 2016-2023 the original author or authors.
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
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.select.MultiSelectDSL
import org.mybatis.dynamic.sql.select.MultiSelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.Messages

typealias MultiSelectCompleter = KotlinMultiSelectBuilder.() -> Unit

@MyBatisDslMarker
class KotlinMultiSelectBuilder: Buildable<MultiSelectModel> {
    private var dsl: MultiSelectDSL? = null
        private set(value) {
            if (field != null) {
                throw KInvalidSQLException(Messages.getString("ERROR.33")) //$NON-NLS-1$
            }
            field = value
        }

    fun select(vararg selectList: BasicColumn, completer: SelectCompleter) =
        select(selectList.asList(), completer)

    fun select(selectList: List<BasicColumn>, completer: SelectCompleter) {
        val b = KotlinSelectBuilder(SqlBuilder.select(selectList)).apply(completer)
        dsl = SqlBuilder.multiSelect(b)
    }

    fun selectDistinct(vararg selectList: BasicColumn, completer: SelectCompleter) =
        selectDistinct(selectList.asList(), completer)

    fun selectDistinct(selectList: List<BasicColumn>, completer: SelectCompleter) {
        val b = KotlinSelectBuilder(SqlBuilder.selectDistinct(selectList)).apply(completer)
        dsl = SqlBuilder.multiSelect(b)
    }

    fun union(completer: KotlinSubQueryBuilder.() -> Unit) {
        val b = KotlinSubQueryBuilder().apply(completer)
        getDsl().union(b)
    }

    fun unionAll(completer: KotlinSubQueryBuilder.() -> Unit) {
        val b = KotlinSubQueryBuilder().apply(completer)
        getDsl().unionAll(b)
    }

    fun orderBy(vararg columns: SortSpecification) {
        getDsl().orderBy(columns.asList())
    }

    fun limit(limit: Long) {
        getDsl().limit(limit)
    }

    fun offset(offset: Long) {
        getDsl().offset(offset)
    }

    fun fetchFirst(fetchFirstRows: Long) {
        getDsl().fetchFirst(fetchFirstRows).rowsOnly()
    }

    override fun build(): MultiSelectModel =
        getDsl().build()

    private fun getDsl(): MultiSelectDSL {
        return dsl?: throw KInvalidSQLException(Messages.getString("ERROR.34")) //$NON-NLS-1$
    }
}
