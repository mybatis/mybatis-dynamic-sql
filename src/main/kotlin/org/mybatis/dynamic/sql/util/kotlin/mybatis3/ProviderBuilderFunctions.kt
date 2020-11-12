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
@file:Suppress("TooManyFunctions")
package org.mybatis.dynamic.sql.util.kotlin.mybatis3

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.util.kotlin.CountColumnCompleter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.MultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.count
import org.mybatis.dynamic.sql.util.kotlin.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.countFrom
import org.mybatis.dynamic.sql.util.kotlin.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.insertInto
import org.mybatis.dynamic.sql.util.kotlin.insertSelect
import org.mybatis.dynamic.sql.util.kotlin.into
import org.mybatis.dynamic.sql.util.kotlin.select
import org.mybatis.dynamic.sql.util.kotlin.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.update

fun count(column: BasicColumn, completer: CountColumnCompleter) =
    count(column, completer).render(RenderingStrategies.MYBATIS3)

fun countDistinct(column: BasicColumn, completer: CountColumnCompleter) =
    countDistinct(column, completer).render(RenderingStrategies.MYBATIS3)

fun countFrom(table: SqlTable, completer: CountCompleter) =
    countFrom(table, completer).render(RenderingStrategies.MYBATIS3)

fun deleteFrom(table: SqlTable, completer: DeleteCompleter) =
    deleteFrom(table, completer).render(RenderingStrategies.MYBATIS3)

fun insertInto(table: SqlTable, completer: GeneralInsertCompleter) =
    insertInto(table, completer).render(RenderingStrategies.MYBATIS3)

fun insertSelect(table: SqlTable, completer: InsertSelectCompleter) =
    insertSelect(table, completer).render(RenderingStrategies.MYBATIS3)

fun <T> InsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: InsertCompleter<T>) =
    into(table, completer).render(RenderingStrategies.MYBATIS3)

fun <T> MultiRowInsertDSL.IntoGatherer<T>.into(table: SqlTable, completer: MultiRowInsertCompleter<T>) =
    into(table, completer).render(RenderingStrategies.MYBATIS3)

fun select(vararg columns: BasicColumn, completer: SelectCompleter) =
    select(columns = columns, completer).render(RenderingStrategies.MYBATIS3)

fun select(columns: List<BasicColumn>, completer: SelectCompleter) =
    select(columns, completer).render(RenderingStrategies.MYBATIS3)

fun selectDistinct(vararg columns: BasicColumn, completer: SelectCompleter) =
    selectDistinct(columns = columns, completer).render(RenderingStrategies.MYBATIS3)

fun selectDistinct(columns: List<BasicColumn>, completer: SelectCompleter) =
    selectDistinct(columns, completer).render(RenderingStrategies.MYBATIS3)

fun update(table: SqlTable, completer: UpdateCompleter) =
    update(table, completer).render(RenderingStrategies.MYBATIS3)
