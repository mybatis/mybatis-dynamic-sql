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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.CountDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

typealias CountCompleter = CountDSL<SelectModel>.() -> Buildable<SelectModel>

fun <T> CountDSL<SelectModel>.where(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
    apply {
        where().and(column, condition, collect)
    }

fun <T> CountDSL<SelectModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>) =
    apply {
        where().and(column, condition)
    }

fun <T> CountDSL<SelectModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
    apply {
        where().and(column, condition, collect)
    }

fun <T> CountDSL<SelectModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
    apply {
        where().or(column, condition)
    }

fun <T> CountDSL<SelectModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
    apply {
        where().or(column, condition, collect)
    }

fun CountDSL<SelectModel>.allRows() = this as Buildable<SelectModel>
