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
        completer(KotlinCountBuilder(SqlBuilder.countColumn(column))).build()

    fun countDistinct(column: BasicColumn, completer: CountCompleter) =
        completer(KotlinCountBuilder(SqlBuilder.countDistinctColumn(column))).build()

    fun countFrom(table: SqlTable, completer: CountCompleter) =
        with(KotlinCountBuilder(SqlBuilder.countColumn(SqlBuilder.constant<Long>("*")))) {
            completer(from(table)).build()
        }

    fun deleteFrom(table: SqlTable, completer: DeleteCompleter) =
        completer(KotlinDeleteBuilder(SqlBuilder.deleteFrom(table))).build()

    fun insertInto(table: SqlTable, completer: GeneralInsertCompleter) =
        completer(GeneralInsertDSL.insertInto(table)).build()

    fun insertSelect(table: SqlTable, completer: InsertSelectCompleter) =
        with(completer(KotlinInsertSelectSubQueryBuilder())) {
            SqlBuilder.insertInto(table)
                .withColumnList(columnList)
                .withSelectStatement(selectBuilder)
                .build()
        }

    fun <T> BatchInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: BatchInsertCompleter<T>) =
        completer(into(table)).build()

    fun <T> InsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: InsertCompleter<T>) =
        completer(into(table)).build()

    fun <T> MultiRowInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: MultiRowInsertCompleter<T>) =
        completer(into(table)).build()

    fun select(vararg columns: BasicColumn, completer: SelectCompleter) =
        select(columns.asList(), completer)

    fun select(columns: List<BasicColumn>, completer: SelectCompleter) =
        completer(KotlinSelectBuilder(SqlBuilder.select(columns))).build()

    fun selectDistinct(vararg columns: BasicColumn, completer: SelectCompleter) =
        selectDistinct(columns.asList(), completer)

    fun selectDistinct(columns: List<BasicColumn>, completer: SelectCompleter) =
        completer(KotlinSelectBuilder(SqlBuilder.selectDistinct(columns))).build()

    fun update(table: SqlTable, completer: UpdateCompleter) =
        completer(KotlinUpdateBuilder(SqlBuilder.update(table))).build()
}
