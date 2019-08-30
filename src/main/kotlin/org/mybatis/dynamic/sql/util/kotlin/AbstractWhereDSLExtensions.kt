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
import org.mybatis.dynamic.sql.where.AbstractWhereDSL

fun <T, M : AbstractWhereDSL<M>> AbstractWhereDSL<M>.and(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): M {
    val collector = CriteriaCollector()
    collect(collector)
    return and(column, condition, collector.criteria)
}

fun <T, M : AbstractWhereDSL<M>> AbstractWhereDSL<M>.or(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): M {
    val collector = CriteriaCollector()
    collect(collector)
    return or(column, condition, collector.criteria)
}
