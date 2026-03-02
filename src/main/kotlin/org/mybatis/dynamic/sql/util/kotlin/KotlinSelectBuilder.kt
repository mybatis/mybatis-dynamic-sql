/*
 *    Copyright 2016-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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
class KotlinSelectBuilder(private val dsl: QueryExpressionDSL<SelectModel>) :
    KotlinJoinOperations<QueryExpressionDSL<SelectModel>>(dsl),
    Buildable<SelectModel>, KotlinPagingDSL {

    fun from(table: SqlTable) {
        dsl.from(table)
    }

    fun from(table: SqlTable, alias: String) {
        dsl.from(table, alias)
    }

    fun from(subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit) {
        val builder = KotlinQualifiedSubQueryBuilder().apply(subQuery)
        val cn = builder.correlationName
        if (cn != null) {
            dsl.from(builder, cn)
        } else {
            dsl.from(builder)
        }
    }

    fun groupBy(vararg columns: BasicColumn) {
        dsl.groupBy(columns.toList())
    }

    fun having(criteria: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteria).let {
            dsl.having(it.initialCriterion, it.subCriteria)
        }

    fun orderBy(vararg columns: SortSpecification) {
        dsl.orderBy(columns.toList())
    }

    override fun limitWhenPresent(limit: Long?) {
        dsl.limitWhenPresent(limit)
    }

    override fun offsetWhenPresent(offset: Long?) {
        dsl.offsetWhenPresent(offset)
    }

    override fun fetchFirstWhenPresent(fetchFirstRows: Long?) {
        dsl.fetchFirstWhenPresent(fetchFirstRows).rowsOnly()
    }

    fun union(union: KotlinUnionBuilder.() -> Unit): Unit =
        union(KotlinUnionBuilder(dsl.union()))

    fun unionAll(unionAll: KotlinUnionBuilder.() -> Unit): Unit =
        unionAll(KotlinUnionBuilder(dsl.unionAll()))

    fun forUpdate() {
        dsl.forUpdate()
    }

    fun forNoKeyUpdate() {
        dsl.forNoKeyUpdate()
    }

    fun forShare() {
        dsl.forShare()
    }

    fun forKeyShare() {
        dsl.forKeyShare()
    }

    fun skipLocked() {
        dsl.skipLocked()
    }

    fun nowait() {
        dsl.nowait()
    }

    override fun build(): SelectModel = dsl.build()
}
