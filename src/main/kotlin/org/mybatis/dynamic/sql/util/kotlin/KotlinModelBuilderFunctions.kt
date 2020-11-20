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
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.BatchInsertDSL
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL

/**
 * Collection of functions that will create various DSL models in a Kotlin native way.
 * They are wrapped in an object as a namespacing technique to avoid collision
 * with the similar functions that build providers for the different rendering
 * strategies.
 */
@Suppress("TooManyFunctions")
object KotlinModelBuilderFunctions {
    fun count(column: BasicColumn, completer: CountCompleter) =
        KotlinCountBuilder(SqlBuilder.countColumn(column)).apply(completer).build()

    fun countDistinct(column: BasicColumn, completer: CountCompleter) =
        KotlinCountBuilder(SqlBuilder.countDistinctColumn(column)).apply(completer).build()

    fun countFrom(table: SqlTable, completer: CountCompleter) =
        KotlinCountBuilder(SqlBuilder.countColumn(SqlBuilder.constant<Long>("*")))
            .from(table).apply(completer).build()

    fun deleteFrom(table: SqlTable, completer: DeleteCompleter) =
        KotlinDeleteBuilder(SqlBuilder.deleteFrom(table)).apply(completer).build()

    fun insertInto(table: SqlTable, completer: GeneralInsertCompleter) =
        GeneralInsertDSL.insertInto(table).apply(completer).build()

    fun insertSelect(table: SqlTable, completer: InsertSelectCompleter) =
        with(KotlinInsertSelectSubQueryBuilder().apply(completer)) {
            SqlBuilder.insertInto(table)
                .withColumnList(columnList)
                .withSelectStatement(this)
                .build()
        }

    fun <T> BatchInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: BatchInsertCompleter<T>) =
        into(table).apply(completer).build()

    fun <T> InsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: InsertCompleter<T>) =
        into(table).apply(completer).build()

    fun <T> MultiRowInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: MultiRowInsertCompleter<T>) =
        into(table).apply(completer).build()

    fun select(vararg columns: BasicColumn, completer: SelectCompleter) =
        select(columns.asList(), completer)

    fun select(columns: List<BasicColumn>, completer: SelectCompleter) =
        KotlinSelectBuilder(SqlBuilder.select(columns)).apply(completer).build()

    fun selectDistinct(vararg columns: BasicColumn, completer: SelectCompleter) =
        selectDistinct(columns.asList(), completer)

    fun selectDistinct(columns: List<BasicColumn>, completer: SelectCompleter) =
        KotlinSelectBuilder(SqlBuilder.selectDistinct(columns)).apply(completer).build()

    fun update(table: SqlTable, completer: UpdateCompleter) =
        KotlinUpdateBuilder(SqlBuilder.update(table)).apply(completer).build()
}
