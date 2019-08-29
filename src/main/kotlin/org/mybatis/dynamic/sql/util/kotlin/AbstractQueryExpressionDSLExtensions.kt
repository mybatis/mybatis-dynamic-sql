/**
 *    Copyright 2016-2019 the original author or authors.
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
        .join(table: SqlTable, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return join(table, collector.onJoinCriterion, collector.andJoinCriteria)
}

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .join(table: SqlTable, alias: String, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return join(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
}

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .fullJoin(table: SqlTable, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return fullJoin(table, collector.onJoinCriterion, collector.andJoinCriteria)
}

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .fullJoin(table: SqlTable, alias: String, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return fullJoin(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
}

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .leftJoin(table: SqlTable, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return leftJoin(table, collector.onJoinCriterion, collector.andJoinCriteria)
}

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .leftJoin(table: SqlTable, alias: String, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return leftJoin(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
}

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .rightJoin(table: SqlTable, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return rightJoin(table, collector.onJoinCriterion, collector.andJoinCriteria)
}

fun <T : AbstractQueryExpressionDSL<T, SelectModel>> AbstractQueryExpressionDSL<T, SelectModel>
        .rightJoin(table: SqlTable, alias: String, collect: JoinReceiver): T {
    val collector = JoinCollector()
    collect(collector)
    return rightJoin(table, alias, collector.onJoinCriterion, collector.andJoinCriteria)
}
