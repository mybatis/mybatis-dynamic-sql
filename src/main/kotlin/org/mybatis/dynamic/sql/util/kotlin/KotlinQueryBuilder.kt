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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

typealias SelectCompleter = KotlinQueryBuilder.() -> Buildable<SelectModel>

@Suppress("TooManyFunctions")
class KotlinQueryBuilder(private val dsl: QueryExpressionDSL<SelectModel>) :
    KotlinBaseJoiningBuilder<QueryExpressionDSL<SelectModel>,
            QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder,
            KotlinQueryBuilder>(dsl), Buildable<SelectModel> {

    fun groupBy(vararg columns: BasicColumn) =
        apply {
            dsl.groupBy(columns.toList())
        }

    fun orderBy(vararg columns: SortSpecification) =
        apply {
            dsl.orderBy(columns.toList())
        }

    fun limit(limit: Long) =
        apply {
            dsl.limit(limit)
        }

    fun offset(offset: Long) =
        apply {
            dsl.offset(offset)
        }

    fun fetchFirst(fetchFirstRows: Long): SelectDSL<SelectModel>.FetchFirstFinisher = dsl.fetchFirst(fetchFirstRows)

    fun allRows() = this

    fun union(union: KotlinUnionBuilder.() -> Unit) =
        apply {
            union(KotlinUnionBuilder(dsl.union()))
        }

    fun unionAll(unionAll: KotlinUnionBuilder.() -> Unit) =
        apply {
            unionAll(KotlinUnionBuilder(dsl.unionAll()))
        }

    override fun build(): SelectModel = dsl.build()

    override fun getWhere(): QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder = dsl.where()

    override fun self() = this
}
