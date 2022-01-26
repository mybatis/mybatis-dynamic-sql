/*
 *    Copyright 2016-2022 the original author or authors.
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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.ExistsCriterion
import org.mybatis.dynamic.sql.NotCriterion
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.VisitableCondition

typealias GroupingCriteriaReceiver = GroupingCriteriaCollector.() -> Unit

/**
 * This class is used to gather criteria for a where clause. The class gathers two types of criteria:
 * an initial criterion, and sub-criteria connected by either an "and" or an "or".
 *
 * An initial criterion can be one of four types:
 * - A column and condition (called with the invoke operator on a column)
 * - An exists operator (called with the "exists" function)
 * - A criteria group which is essentially parenthesis within the where clause (called with the group function)
 * - A "not" group (called with the not function)
 *
 * Only one of these initial criterion functions should be called within each scope. If you need more than one,
 * use a sub-criteria joined with "and" or "or"
 */
@MyBatisDslMarker
class GroupingCriteriaCollector {
    internal var initialCriterion: SqlCriterion? = null
    internal val subCriteria = mutableListOf<AndOrCriteriaGroup>()

    fun and(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.subCriteria.add(
                AndOrCriteriaGroup.Builder().withConnector("and")
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build()
            )
        }

    fun or(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.subCriteria.add(
                AndOrCriteriaGroup.Builder().withConnector("or")
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build()
            )
        }

    /**
     * This should only be specified once per scope, and should be first
     */
    fun not(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.initialCriterion = NotCriterion.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }

    /**
     * This should only be specified once per scope, and should be first
     */
    fun exists(kotlinSubQueryBuilder: KotlinSubQueryBuilder.() -> Unit): Unit =
        with(KotlinSubQueryBuilder().apply(kotlinSubQueryBuilder)) {
            this@GroupingCriteriaCollector.initialCriterion =
                ExistsCriterion.Builder().withExistsPredicate(SqlBuilder.exists(this)).build()
        }

    /**
     * This could "almost" be an operator invoke function. The problem is that
     * to call it a user would need to use "this" explicitly. I think that is too
     * confusing, so we'll stick with the function name of "group"
     *
     * This should only be specified once per scope, and should be first
     */
    fun group(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        with(GroupingCriteriaCollector().apply(criteriaReceiver)) {
            this@GroupingCriteriaCollector.initialCriterion = CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }

    /**
     * Build an initial criterion for a where clause, or a nested and/or/not group.
     * You can use it like A (isEqualTo(3))
     */
    infix operator fun <T : Any> BindableColumn<T>.invoke(condition: VisitableCondition<T>) {
        initialCriterion = ColumnAndConditionCriterion.withColumn(this)
            .withCondition(condition)
            .build()
    }
}
