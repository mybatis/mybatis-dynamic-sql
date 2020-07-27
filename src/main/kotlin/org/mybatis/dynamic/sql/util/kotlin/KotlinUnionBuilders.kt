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
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel

class KotlinUnionBuilder(private val dsl: QueryExpressionDSL<SelectModel>) {
    fun select(vararg selectList: BasicColumn) =
        select(listOf(*selectList))

    fun select(selectList: List<BasicColumn>) =
        KotlinUnionFromGatherer(dsl, selectList)
}

class KotlinUnionFromGatherer(
    private val dsl: QueryExpressionDSL<SelectModel>,
    private val selectList: List<BasicColumn>
) {
    fun from(
        table: SqlTable,
        enhance: KotlinUnionQueryBuilder.() -> KotlinUnionQueryBuilder
    ): QueryExpressionDSL<SelectModel> {
        val unionBuilder = KotlinUnionQueryBuilder(dsl.union().select(selectList).from(table))
        enhance(unionBuilder)
        return dsl
    }

    fun from(
        table: SqlTable,
        alias: String,
        enhance: KotlinUnionQueryBuilder.() -> KotlinUnionQueryBuilder
    ): QueryExpressionDSL<SelectModel> {
        val unionBuilder = KotlinUnionQueryBuilder(dsl.union().select(selectList).from(table, alias))
        enhance(unionBuilder)
        return dsl
    }
}

class KotlinUnionQueryBuilder(private val dsl: QueryExpressionDSL<SelectModel>) :
    KotlinBaseJoiningBuilder<QueryExpressionDSL<SelectModel>, QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder,
            KotlinUnionQueryBuilder>(dsl) {
    override fun self() = this

    override fun getWhere(): QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder = dsl.where()
}
