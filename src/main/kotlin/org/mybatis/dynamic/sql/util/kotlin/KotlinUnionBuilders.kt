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

@MyBatisDslMarker
class KotlinUnionBuilder(private val unionBuilder: QueryExpressionDSL<SelectModel>.UnionBuilder) {
    fun select(vararg selectList: BasicColumn) =
        select(selectList.toList())

    fun select(selectList: List<BasicColumn>) =
        KotlinUnionFromGatherer(unionBuilder.select(selectList))

    fun selectDistinct(vararg selectList: BasicColumn) =
        selectDistinct(selectList.toList())

    fun selectDistinct(selectList: List<BasicColumn>) =
        KotlinUnionFromGatherer(unionBuilder.selectDistinct(selectList))
}

class KotlinUnionFromGatherer(private val fromGatherer: QueryExpressionDSL.FromGatherer<SelectModel>) {
    fun from(table: SqlTable, enhance: KotlinUnionQueryBuilder.() -> Unit) =
        enhance(KotlinUnionQueryBuilder(fromGatherer.from(table)))

    fun from(table: SqlTable, alias: String, enhance: KotlinUnionQueryBuilder.() -> Unit) =
        enhance(KotlinUnionQueryBuilder(fromGatherer.from(table, alias)))
}

class KotlinUnionQueryBuilder(private val unionDsl: QueryExpressionDSL<SelectModel>) :
    KotlinBaseJoiningBuilder<QueryExpressionDSL<SelectModel>,
            QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder,
            KotlinUnionQueryBuilder>(unionDsl) {
    fun allRows() = this

    override fun self() = this

    override fun getWhere(): QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder = unionDsl.where()
}
