/*
 *    Copyright 2016-2022 the original author or authors.
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

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.SubQuery
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.Messages

typealias SelectCompleter = KotlinSelectBuilder.() -> Unit

@Suppress("TooManyFunctions")
class KotlinSelectBuilder(private val fromGatherer: QueryExpressionDSL.FromGatherer<SelectModel>) :
    KotlinBaseJoiningBuilder<QueryExpressionDSL<SelectModel>>(), Buildable<SelectModel> {

    private var dsl: KQueryExpressionDSL? = null

    fun from(table: SqlTable) {
        dsl = KQueryExpressionDSL(fromGatherer, table)
    }

    fun from(table: SqlTable, alias: String) {
        dsl = KQueryExpressionDSL(fromGatherer, table, alias)
    }

    fun from(subQuery: KotlinQualifiedSubQueryBuilder.() -> Unit) {
        val builder = KotlinQualifiedSubQueryBuilder().apply(subQuery)
        dsl = KQueryExpressionDSL(fromGatherer, builder)
    }

    fun groupBy(vararg columns: BasicColumn) {
        getDsl().groupBy(columns.toList())
    }

    fun having(criteria: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteria).let {
            getDsl().applyHaving(it.initialCriterion, it.subCriteria)
        }

    fun orderBy(vararg columns: SortSpecification) {
        getDsl().orderBy(columns.toList())
    }

    fun limit(limit: Long) {
        getDsl().limit(limit)
    }

    fun offset(offset: Long) {
        getDsl().offset(offset)
    }

    fun fetchFirst(fetchFirstRows: Long) {
        getDsl().fetchFirst(fetchFirstRows).rowsOnly()
    }

    fun union(union: KotlinUnionBuilder.() -> Unit): Unit =
        union(KotlinUnionBuilder(getDsl().union()))

    fun unionAll(unionAll: KotlinUnionBuilder.() -> Unit): Unit =
        unionAll(KotlinUnionBuilder(getDsl().unionAll()))

    override fun build(): SelectModel = getDsl().build()

    override fun getDsl(): KQueryExpressionDSL {
        return dsl?: throw KInvalidSQLException(Messages.getString("ERROR.27")) //$NON-NLS-1$
    }
}

/**
 * Extension of the QueryExpressionDSL class that provides access to protected methods in that class.
 * We do this especially for having support because we don't want to publicly expose a "having" method
 * directly in QueryExpressionDSL as it would be in an odd place for the Java DSL.
 */
class KQueryExpressionDSL: QueryExpressionDSL<SelectModel> {
    constructor(fromGatherer: FromGatherer<SelectModel>, table: SqlTable) : super(fromGatherer, table)

    constructor(fromGatherer: FromGatherer<SelectModel>, table: SqlTable, alias: String) :
            super(fromGatherer, table, alias)

    constructor(fromGatherer: FromGatherer<SelectModel>, subQuery: KotlinQualifiedSubQueryBuilder) :
            super(fromGatherer, buildSubQuery(subQuery))

    internal fun applyHaving(initialCriterion: SqlCriterion?, subCriteria: List<AndOrCriteriaGroup>) {
        having(initialCriterion, subCriteria)
    }

    companion object {
        fun buildSubQuery(subQuery: KotlinQualifiedSubQueryBuilder): SubQuery =
            with(SubQuery.Builder()) {
                withSelectModel(subQuery.build())
                withAlias(subQuery.correlationName)
                build()
            }
    }
}
