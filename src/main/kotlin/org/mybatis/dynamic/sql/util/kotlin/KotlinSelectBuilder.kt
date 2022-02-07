/*
 *    Copyright 2016-2021 the original author or authors.
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
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

typealias SelectCompleter = KotlinSelectBuilder.() -> Unit

@Suppress("TooManyFunctions")
class KotlinSelectBuilder(private val fromGatherer: QueryExpressionDSL.FromGatherer<SelectModel>) :
    KotlinBaseJoiningBuilder<QueryExpressionDSL<SelectModel>>(), Buildable<SelectModel> {

    private lateinit var dsl: QueryExpressionDSL<SelectModel>

    fun from(table: SqlTable): KotlinSelectBuilder =
        apply {
            dsl = fromGatherer.from(table)
        }

    fun from(table: SqlTable, alias: String): KotlinSelectBuilder =
        apply {
            dsl = fromGatherer.from(table, alias)
        }

    fun from(subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit): KotlinSelectBuilder =
        apply {
            val builder = KotlinQualifiedSubQueryBuilder().apply(subQuery)
            dsl = fromGatherer.from(builder, builder.correlationName)
        }

    fun groupBy(vararg columns: BasicColumn): KotlinSelectBuilder =
        apply {
            getDsl().groupBy(columns.toList())
        }

    fun orderBy(vararg columns: SortSpecification): KotlinSelectBuilder =
        apply {
            getDsl().orderBy(columns.toList())
        }

    fun limit(limit: Long): KotlinSelectBuilder =
        apply {
            getDsl().limit(limit)
        }

    fun offset(offset: Long): KotlinSelectBuilder =
        apply {
            getDsl().offset(offset)
        }

    fun fetchFirst(fetchFirstRows: Long): KotlinSelectBuilder =
        apply {
            getDsl().fetchFirst(fetchFirstRows).rowsOnly()
        }

    fun union(union: KotlinUnionBuilder.() -> Unit): KotlinSelectBuilder =
        apply {
            union(KotlinUnionBuilder(getDsl().union()))
        }

    fun unionAll(unionAll: KotlinUnionBuilder.() -> Unit): KotlinSelectBuilder =
        apply {
            unionAll(KotlinUnionBuilder(getDsl().unionAll()))
        }

    override fun build(): SelectModel = getDsl().build()

    override fun getDsl(): QueryExpressionDSL<SelectModel> {
        try {
            return dsl
        } catch (e: UninitializedPropertyAccessException) {
            throw UninitializedPropertyAccessException(
                "You must specify a \"from\" clause before any other clauses in a select statement",
                e
            )
        }
    }
}
