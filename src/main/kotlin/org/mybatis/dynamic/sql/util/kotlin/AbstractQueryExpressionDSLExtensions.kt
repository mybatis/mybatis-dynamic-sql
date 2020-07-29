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

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.AbstractQueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .join(table: SqlTable, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return join(table, onJoinCriterion, andJoinCriteria)
    }

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .join(table: SqlTable, alias: String, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return join(table, alias, onJoinCriterion, andJoinCriteria)
    }

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .fullJoin(table: SqlTable, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return fullJoin(table, onJoinCriterion, andJoinCriteria)
    }

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .fullJoin(table: SqlTable, alias: String, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return fullJoin(table, alias, onJoinCriterion, andJoinCriteria)
    }

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .leftJoin(table: SqlTable, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return leftJoin(table, onJoinCriterion, andJoinCriteria)
    }

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .leftJoin(table: SqlTable, alias: String, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return leftJoin(table, alias, onJoinCriterion, andJoinCriteria)
    }

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .rightJoin(table: SqlTable, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return rightJoin(table, onJoinCriterion, andJoinCriteria)
    }

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .rightJoin(table: SqlTable, alias: String, collect: JoinReceiver): T =
    with(collect(JoinCollector())) {
        return rightJoin(table, alias, onJoinCriterion, andJoinCriteria)
    }
