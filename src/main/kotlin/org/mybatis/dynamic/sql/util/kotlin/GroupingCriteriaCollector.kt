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

@MyBatisDslMarker
class GroupingCriteriaCollector {
    internal var initialCriterion: SqlCriterion? = null
    internal val subCriteria = mutableListOf<AndOrCriteriaGroup>()

    fun and(criteriaReceiver: GroupingCriteriaReceiver) {
        val cc = GroupingCriteriaCollector().apply(criteriaReceiver)
        subCriteria.add(
            AndOrCriteriaGroup.Builder().withConnector("and")
                .withInitialCriterion(cc.initialCriterion)
                .withSubCriteria(cc.subCriteria)
                .build()
        )
    }

    fun or(criteriaReceiver: GroupingCriteriaReceiver) {
        val cc = GroupingCriteriaCollector().apply(criteriaReceiver)
        subCriteria.add(
            AndOrCriteriaGroup.Builder().withConnector("or")
                .withInitialCriterion(cc.initialCriterion)
                .withSubCriteria(cc.subCriteria)
                .build()
        )
    }

    /**
     * This should only be specified once per scope, and should be first
     */
    fun not(criteriaReceiver: GroupingCriteriaReceiver) {
        val cc = GroupingCriteriaCollector().apply(criteriaReceiver)
        initialCriterion = NotCriterion.Builder()
            .withInitialCriterion(cc.initialCriterion)
            .withSubCriteria(cc.subCriteria)
            .build()
    }

    /**
     * This should only be specified once per scope, and should be first
     */
    fun exists(kotlinSubQueryBuilder: KotlinSubQueryBuilder.() -> Unit) {
        val f = KotlinSubQueryBuilder().apply(kotlinSubQueryBuilder)
        initialCriterion = ExistsCriterion.Builder().withExistsPredicate(SqlBuilder.exists(f)).build()
    }

    /**
     * This could "almost" be an operator invoke function. The problem is that
     * to call it a user would need to use "this" explicitly. I think that is too
     * confusing, so we'll stick with the function name of "group"
     *
     * This should only be specified once per scope, and should be first
     */
    fun group(criteriaReceiver: GroupingCriteriaReceiver) {
        val cc = GroupingCriteriaCollector().apply(criteriaReceiver)
        initialCriterion = CriteriaGroup.Builder()
            .withInitialCriterion(cc.initialCriterion)
            .withSubCriteria(cc.subCriteria)
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
