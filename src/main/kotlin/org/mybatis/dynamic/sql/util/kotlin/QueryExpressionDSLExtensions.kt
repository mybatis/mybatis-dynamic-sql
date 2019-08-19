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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.CompletableQuery
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.join.JoinCondition
import org.mybatis.dynamic.sql.util.Buildable

fun QueryExpressionDSL<SelectModel>.JoinSpecificationStarter.on(joinColumn: BasicColumn, joinCondition: JoinCondition,
                                                                complete: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>) =
        complete(on(joinColumn, joinCondition)).build().render(RenderingStrategies.MYBATIS3)

fun QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher.and(joinColumn: BasicColumn, joinCondition: JoinCondition,
                                                                  complete: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>) =
        complete(and(joinColumn, joinCondition)).build().render(RenderingStrategies.MYBATIS3)

fun QueryExpressionDSL.FromGatherer<SelectModel>.from(table: SqlTable,
                                                      complete: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>) =
        complete(from(table)).build().render(RenderingStrategies.MYBATIS3)
