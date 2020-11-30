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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.ExistsCriterion
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.where.condition.Exists

typealias CriteriaReceiver = CriteriaCollector.() -> Unit

@MyBatisDslMarker
class CriteriaCollector {
    val criteria = mutableListOf<SqlCriterion>()

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        apply {
            criteria.add(
                ColumnAndConditionCriterion.withColumn(column)
                    .withCondition(condition)
                    .withConnector("and")
                    .build()
            )
        }

    fun <T> and(
        column: BindableColumn<T>,
        condition: VisitableCondition<T>,
        criteriaReceiver: CriteriaReceiver
    ) =
        apply {
            criteria.add(
                ColumnAndConditionCriterion.withColumn(column)
                    .withCondition(condition)
                    .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                    .withConnector("and")
                    .build()
            )
        }

    fun and(exists: Exists) =
        apply {
            criteria.add(
                ExistsCriterion.Builder()
                    .withConnector("and")
                    .withExists(exists)
                    .build()
            )
        }

    fun and(exists: Exists, criteriaReceiver: CriteriaReceiver) =
        apply {
            criteria.add(
                ExistsCriterion.Builder()
                    .withConnector("and")
                    .withExists(exists)
                    .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                    .build()
            )
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
        apply {
            criteria.add(
                ColumnAndConditionCriterion.withColumn(column)
                    .withCondition(condition)
                    .withConnector("or")
                    .build()
            )
        }

    fun <T> or(
        column: BindableColumn<T>,
        condition: VisitableCondition<T>,
        criteriaReceiver: CriteriaReceiver
    ) =
        apply {
            criteria.add(
                ColumnAndConditionCriterion.withColumn(column)
                    .withCondition(condition)
                    .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                    .withConnector("or")
                    .build()
            )
        }

    fun or(exists: Exists) =
        apply {
            criteria.add(
                ExistsCriterion.Builder()
                    .withConnector("or")
                    .withExists(exists)
                    .build()
            )
        }

    fun or(exists: Exists, criteriaReceiver: CriteriaReceiver) =
        apply {
            criteria.add(
                ExistsCriterion.Builder()
                    .withConnector("or")
                    .withExists(exists)
                    .withSubCriteria(CriteriaCollector().apply(criteriaReceiver).criteria)
                    .build()
            )
        }
}
