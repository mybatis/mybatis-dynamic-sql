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
