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
import org.mybatis.dynamic.sql.ExistsPredicate
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.VisitableCondition

typealias CriteriaReceiver = CriteriaCollector.() -> Unit

@MyBatisDslMarker
class CriteriaCollector {
    val criteria = mutableListOf<AndOrCriteriaGroup>()

    fun <T> and(
        column: BindableColumn<T>,
        condition: VisitableCondition<T>,
        criteriaReceiver: CriteriaReceiver = {}
    ): CriteriaCollector =
        addCriteriaGroup("and", buildCriterion(column, condition), criteriaReceiver)

    fun and(existsPredicate: ExistsPredicate, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        addCriteriaGroup("and", buildCriterion(existsPredicate), criteriaReceiver)

    fun and(criteriaGroup: CriteriaGroup, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        addCriteriaGroup("and", buildCriterion(criteriaGroup), criteriaReceiver)

    fun <T> or(
        column: BindableColumn<T>,
        condition: VisitableCondition<T>,
        criteriaReceiver: CriteriaReceiver = {}
    ): CriteriaCollector =
        addCriteriaGroup("or", buildCriterion(column, condition), criteriaReceiver)

    fun or(existsPredicate: ExistsPredicate, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        addCriteriaGroup("or", buildCriterion(existsPredicate), criteriaReceiver)

    fun or(criteriaGroup: CriteriaGroup, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        addCriteriaGroup("or", buildCriterion(criteriaGroup), criteriaReceiver)

    private fun <T> buildCriterion(
        column: BindableColumn<T>,
        condition: VisitableCondition<T>
    ): ColumnAndConditionCriterion<T> =
        ColumnAndConditionCriterion.withColumn(column).withCondition(condition).build()

    private fun buildCriterion(existsPredicate: ExistsPredicate): ExistsCriterion =
        ExistsCriterion.Builder().withExistsPredicate(existsPredicate).build()

    private fun buildCriterion(criteriaGroup: CriteriaGroup): CriteriaGroup =
        CriteriaGroup.Builder().withInitialCriterion(criteriaGroup).build()

    private fun addCriteriaGroup(
        connector: String,
        initialCriterion: SqlCriterion,
        criteriaReceiver: CriteriaReceiver
    ) =
        apply {
            criteria.add(
                AndOrCriteriaGroup.Builder()
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                    .withConnector(connector)
                    .build()
            )
        }
}
