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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.CriteriaGroupWithConnector
import org.mybatis.dynamic.sql.ExistsCriterion
import org.mybatis.dynamic.sql.ExistsPredicate
import org.mybatis.dynamic.sql.VisitableCondition

typealias CriteriaReceiver = CriteriaCollector.() -> Unit

@MyBatisDslMarker
class CriteriaCollector {
    val criteria = mutableListOf<CriteriaGroupWithConnector>()

    fun <T> and(
        column: BindableColumn<T>,
        condition: VisitableCondition<T>,
        criteriaReceiver: CriteriaReceiver = {}
    ): CriteriaCollector =
        apply {
            criteria.add(CriteriaGroupWithConnector.Builder()
                .withInitialCriterion(ColumnAndConditionCriterion.withColumn(column)
                    .withCondition(condition)
                    .build())
                .withConnector("and")
                .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                .build()
            )
        }

    fun and(existsPredicate: ExistsPredicate, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        apply {
            criteria.add(CriteriaGroupWithConnector.Builder()
                .withInitialCriterion(ExistsCriterion.Builder()
                    .withExistsPredicate(existsPredicate)
                    .build())
                .withConnector("and")
                .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                .build()
            )
        }

    fun and(criteriaGroup: CriteriaGroup, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        apply {
            criteria.add(CriteriaGroupWithConnector.Builder()
                .withInitialCriterion(CriteriaGroup.Builder()
                    .withInitialCriterion(criteriaGroup)
                    .build())
                .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                .withConnector("and")
                .build()
            )
        }

    fun <T> or(
        column: BindableColumn<T>,
        condition: VisitableCondition<T>,
        criteriaReceiver: CriteriaReceiver = {}
    ): CriteriaCollector =
        apply {
            criteria.add(CriteriaGroupWithConnector.Builder()
                .withInitialCriterion(ColumnAndConditionCriterion.withColumn(column)
                    .withCondition(condition)
                    .build())
                .withConnector("or")
                .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                .build()
            )
        }

    fun or(existsPredicate: ExistsPredicate, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        apply {
            criteria.add(CriteriaGroupWithConnector.Builder()
                .withInitialCriterion(ExistsCriterion.Builder()
                    .withExistsPredicate(existsPredicate)
                    .build())
                .withConnector("or")
                .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                .build()
            )
        }

    fun or(criteriaGroup: CriteriaGroup, criteriaReceiver: CriteriaReceiver = {}): CriteriaCollector =
        apply {
            criteria.add(CriteriaGroupWithConnector.Builder()
                .withInitialCriterion(CriteriaGroup.Builder()
                    .withInitialCriterion(criteriaGroup)
                    .build())
                .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                .withConnector("or")
                .build()
            )
        }
}
