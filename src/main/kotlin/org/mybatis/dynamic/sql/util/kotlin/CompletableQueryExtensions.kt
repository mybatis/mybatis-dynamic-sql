package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.CompletableQuery
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

fun <T> CompletableQuery<SelectModel>.where(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                            collect: CriteriaCollector.() -> CriteriaCollector) =
        apply {
            val collector = CriteriaCollector()
            collect(collector)
            where(column, condition, *collector.criteria())
        }

fun <T> CompletableQuery<SelectModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                          collect: CriteriaCollector.() -> CriteriaCollector) =
        apply {
            val collector = CriteriaCollector()
            collect(collector)
            and(column, condition, *collector.criteria())
        }

fun <T> CompletableQuery<SelectModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                         collect: CriteriaCollector.() -> CriteriaCollector) =
        apply {
            val collector = CriteriaCollector()
            collect(collector)
            or(column, condition, *collector.criteria())
        }

fun CompletableQuery<SelectModel>.allRows() = this as Buildable<SelectModel>
