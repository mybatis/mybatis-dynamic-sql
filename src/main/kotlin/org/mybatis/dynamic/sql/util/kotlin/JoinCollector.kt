/**
 *    Copyright 2016-2019 the original author or authors.
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
import org.mybatis.dynamic.sql.select.join.AndJoinCriterion
import org.mybatis.dynamic.sql.select.join.JoinCondition
import org.mybatis.dynamic.sql.select.join.OnJoinCriterion

class JoinCollector {
    lateinit var onJoinCriterion: OnJoinCriterion
    var andJoinCriteria = mutableListOf<AndJoinCriterion>()

    fun on(column: BasicColumn, condition: JoinCondition) =
            apply {
                onJoinCriterion = OnJoinCriterion.Builder()
                        .withJoinColumn(column)
                        .withJoinCondition(condition)
                        .build()
            }

    fun and(column: BasicColumn, condition: JoinCondition) =
            apply {
                andJoinCriteria.add(AndJoinCriterion.Builder()
                        .withJoinColumn(column)
                        .withJoinCondition(condition)
                        .build())

            }

    fun andJoinCriteria() = andJoinCriteria.toTypedArray()
}
