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

import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.InsertModel
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.kotlin.spring.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.spring.into
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

typealias GeneralInsertCompleter = GeneralInsertDSL.() -> Buildable<GeneralInsertModel>

typealias InsertCompleter<T> = InsertDSL<T>.() -> Buildable<InsertModel<T>>

typealias MultiRowInsertCompleter<T> = MultiRowInsertDSL<T>.() -> Buildable<MultiRowInsertModel<T>>

@MyBatisDslMarker
class MultiRowInsertHelper<T>(private val records: List<T>, private val template: NamedParameterJdbcTemplate) {
    fun into(table: SqlTable, completer: MultiRowInsertCompleter<T>) =
        template.insertMultiple(SqlBuilder.insertMultiple(records).into(table, completer))
}

