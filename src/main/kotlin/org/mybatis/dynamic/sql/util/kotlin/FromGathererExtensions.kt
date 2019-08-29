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
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel

typealias QueryExpressionEnhancer = QueryExpressionDSL<SelectModel>.() -> QueryExpressionDSL<SelectModel>

// These functions are intended for use in a Join mapper where a join is setup before the remainder
// of the query is completed
fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable, enhancer: QueryExpressionEnhancer) =
    enhancer(from(table))

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable, alias: String, enhancer: QueryExpressionEnhancer) =
    enhancer(from(table, alias))
