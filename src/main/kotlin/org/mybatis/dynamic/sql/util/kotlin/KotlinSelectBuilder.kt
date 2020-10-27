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
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.select.QueryExpressionDSL

typealias SelectCompleter = KotlinSelectBuilder.() -> KotlinSelectBuilder

// convenience methods for building partials
fun select(basicColumns: List<BasicColumn>, complete: SelectCompleter) =
    complete(KotlinSelectBuilder(SqlBuilder.select(basicColumns)))

fun selectDistinct(basicColumns: List<BasicColumn>, complete: SelectCompleter) =
    complete(KotlinSelectBuilder(SqlBuilder.selectDistinct(basicColumns)))

@Suppress("TooManyFunctions")
class KotlinSelectBuilder(private val fromGatherer: QueryExpressionDSL.FromGatherer<SelectModel>) :
    KotlinBaseJoiningBuilder<QueryExpressionDSL<SelectModel>, QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder , KotlinSelectBuilder>(), Buildable<SelectModel> {

    private lateinit var dsl: QueryExpressionDSL<SelectModel>

    fun from(table: SqlTable) =
        apply {
            dsl = fromGatherer.from(table)
        }

    fun from(table: SqlTable, alias: String) =
        apply {
            dsl = fromGatherer.from(table, alias)
        }

    fun groupBy(vararg columns: BasicColumn) =
        apply {
            getDsl().groupBy(columns.toList())
        }

    fun orderBy(vararg columns: SortSpecification) =
        apply {
            getDsl().orderBy(columns.toList())
        }

    fun limit(limit: Long) =
        apply {
            getDsl().limit(limit)
        }

    fun offset(offset: Long) =
        apply {
            getDsl().offset(offset)
        }

    fun fetchFirst(fetchFirstRows: Long) =
        apply {
            getDsl().fetchFirst(fetchFirstRows).rowsOnly()
        }

    fun union(union: KotlinUnionBuilder.() -> Unit) =
        apply {
            union(KotlinUnionBuilder(getDsl().union()))
        }

    fun unionAll(unionAll: KotlinUnionBuilder.() -> Unit) =
        apply {
            unionAll(KotlinUnionBuilder(getDsl().unionAll()))
        }

    override fun build() = getDsl().build()

    override fun self(): KotlinSelectBuilder = this

    override fun getWhere(): QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder = getDsl().where()

    override fun getDsl() : QueryExpressionDSL<SelectModel> {
        try {
            return dsl
        } catch (e: UninitializedPropertyAccessException) {
            throw UninitializedPropertyAccessException("You must specify a \"from\" clause before any other clauses")
        }
    }
}
