/*
 *    Copyright 2016-2024 the original author or authors.
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
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.VisitableCondition

typealias JoinReceiver = JoinCollector.() -> Unit

@MyBatisDslMarker
class JoinCollector {
    private var onJoinCriterion: SqlCriterion? = null
    internal val andJoinCriteria = mutableListOf<AndOrCriteriaGroup>()

    internal fun onJoinCriterion() : SqlCriterion = invalidIfNull(onJoinCriterion, "ERROR.22") //$NON-NLS-1$

    fun <T> on(leftColumn: BindableColumn<T>): RightColumnCollector<T> = RightColumnCollector {
        onJoinCriterion = ColumnAndConditionCriterion.withColumn(leftColumn)
            .withCondition(it)
            .build()
    }

    fun <T> and(leftColumn: BindableColumn<T>): RightColumnCollector<T> = RightColumnCollector {
        andJoinCriteria.add(
            AndOrCriteriaGroup.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withInitialCriterion(ColumnAndConditionCriterion.withColumn(leftColumn).withCondition(it).build())
                .build()
        )
    }
}

class RightColumnCollector<T>(private val joinConditionConsumer: (VisitableCondition<T>) -> Unit) {
    infix fun equalTo(rightColumn: BindableColumn<T>) = joinConditionConsumer.invoke(SqlBuilder.equalTo(rightColumn))

    infix fun equalTo(value: T) = joinConditionConsumer.invoke(SqlBuilder.equalTo(value))
}
