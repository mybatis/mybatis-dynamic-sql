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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.select.join.JoinCondition
import org.mybatis.dynamic.sql.select.join.JoinCriterion
import org.mybatis.dynamic.sql.util.Messages

typealias JoinReceiver = JoinCollector.() -> Unit

@MyBatisDslMarker
class JoinCollector {
    private var onJoinCriterion: JoinCriterion? = null
    internal val andJoinCriteria = mutableListOf<JoinCriterion>()

    internal fun onJoinCriterion() : JoinCriterion =
        onJoinCriterion?: throw KInvalidSQLException(Messages.getString("ERROR.22")) //$NON-NLS-1$

    fun on(leftColumn: BasicColumn): RightColumnCollector = RightColumnCollector {
        onJoinCriterion = JoinCriterion.Builder()
            .withConnector("on") //$NON-NLS-1$
            .withJoinColumn(leftColumn)
            .withJoinCondition(it)
            .build()
    }

    fun and(leftColumn: BasicColumn): RightColumnCollector = RightColumnCollector {
        andJoinCriteria.add(
            JoinCriterion.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withJoinColumn(leftColumn)
                .withJoinCondition(it)
                .build()
        )
    }

    @Deprecated("Please use: on(leftColumn) equalTo rightColumn")
    fun on(column: BasicColumn, condition: JoinCondition) {
        onJoinCriterion = JoinCriterion.Builder()
            .withConnector("on") //$NON-NLS-1$
            .withJoinColumn(column)
            .withJoinCondition(condition)
            .build()
    }

    @Deprecated("Please use: and(leftColumn) equalTo rightColumn")
    fun and(column: BasicColumn, condition: JoinCondition) {
        andJoinCriteria.add(
            JoinCriterion.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withJoinColumn(column)
                .withJoinCondition(condition)
                .build()
        )
    }
}

class RightColumnCollector(private val joinConditionConsumer: (JoinCondition) -> Unit) {
    infix fun equalTo(rightColumn: BasicColumn) = joinConditionConsumer.invoke(SqlBuilder.equalTo(rightColumn))
}
