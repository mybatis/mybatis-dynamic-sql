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
import org.mybatis.dynamic.sql.delete.DeleteDSL
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.util.Buildable

typealias DeleteCompleter = DeleteDSL<DeleteModel>.() -> Buildable<DeleteModel>

fun <T> DeleteDSL<DeleteModel>.where(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
    apply {
        where().and(column, condition, collect)
    }

fun <T> DeleteDSL<DeleteModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>) =
    apply {
        where().and(column, condition)
    }

fun <T> DeleteDSL<DeleteModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
    apply {
        where().and(column, condition, collect)
    }

fun <T> DeleteDSL<DeleteModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>) =
    apply {
        where().or(column, condition)
    }

fun <T> DeleteDSL<DeleteModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver) =
    apply {
        where().or(column, condition, collect)
    }

fun DeleteDSL<DeleteModel>.allRows() = this as Buildable<DeleteModel>
