/*
 *    Copyright 2016-2022 the original author or authors.
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

import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.VisitableCondition

class InfixBetweenBuilder<T> (private val value1: T, private val conditionConsumer: (VisitableCondition<T>) -> Unit) {
    infix fun and(value2: T) = conditionConsumer.invoke(SqlBuilder.isBetween(value1).and(value2))
}

class InfixBetweenWhenPresentBuilder<T>(
    private val value1: T?,
    private val conditionConsumer: (VisitableCondition<T>) -> Unit
) {
    infix fun and(value2: T?) = conditionConsumer.invoke(SqlBuilder.isBetweenWhenPresent<T>(value1).and(value2))
}

class InfixNotBetweenBuilder<T>(
    private val value1: T,
    private val conditionConsumer: (VisitableCondition<T>) -> Unit
) {
    infix fun and(value2: T) = conditionConsumer.invoke(SqlBuilder.isNotBetween(value1).and(value2))
}

class InfixNotBetweenWhenPresentBuilder<T>(
    private val value1: T?,
    private val conditionConsumer: (VisitableCondition<T>) -> Unit
) {
    infix fun and(value2: T?) = conditionConsumer.invoke(SqlBuilder.isNotBetweenWhenPresent<T>(value1).and(value2))
}
