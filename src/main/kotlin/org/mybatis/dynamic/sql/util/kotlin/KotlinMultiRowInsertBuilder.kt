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

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.Messages

typealias KotlinMultiRowInsertCompleter<T> = KotlinMultiRowInsertBuilder<T>.() -> Unit

@MyBatisDslMarker
class KotlinMultiRowInsertBuilder<T : Any> (private val rows: Collection<T>): Buildable<MultiRowInsertModel<T>> {
    private var table: SqlTable? = null
    private val columnMappings = mutableListOf<AbstractColumnMapping>()

    fun into(table: SqlTable) {
        this.table = table
    }

    fun <C> map(column: SqlColumn<C>) = MultiRowInsertColumnMapCompleter(column) {
        columnMappings.add(it)
    }

    override fun build(): MultiRowInsertModel<T> =
        if (table == null) {
            throw KInvalidSQLException(Messages.getString("ERROR.26")) //$NON-NLS-1$
        } else {
            with(MultiRowInsertDSL.Builder<T>()) {
                withRecords(rows)
                withTable(table)
                withColumnMappings(columnMappings)
                build()
            }.build()
        }
}
