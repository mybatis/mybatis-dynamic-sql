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
package org.mybatis.dynamic.sql.util.kotlin.elements

import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.SearchedCaseModel.SearchedWhenCondition
import org.mybatis.dynamic.sql.select.SimpleCaseModel.SimpleWhenCondition
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaCollector
import org.mybatis.dynamic.sql.util.kotlin.assertNull

class KSearchedCaseDSL {
    internal var elseValue: String? = null
        private set(value) {
            assertNull(field, "ERROR.42") //$NON-NLS-1$
            field = value
        }
    internal val whenConditions = mutableListOf<SearchedWhenCondition>()

    fun `when`(dslCompleter: SearchedCaseCriteriaCollector.() -> Unit) {
        val dsl = SearchedCaseCriteriaCollector().apply(dslCompleter)
        whenConditions.add(SearchedWhenCondition(dsl.initialCriterion, dsl.subCriteria, dsl.thenValue))
    }

    fun `else`(value: String) {
        this.elseValue = value
    }
}

class SearchedCaseCriteriaCollector : GroupingCriteriaCollector() {
    internal var thenValue: String? = null
        private set(value) {
            assertNull(field, "ERROR.41") //$NON-NLS-1$
            field = value
        }

    fun then(value: String) {
        this.thenValue = value
    }
}

class KSimpleCaseDSL<T : Any> {
    internal var elseValue: String? = null
        private set(value) {
            assertNull(field, "ERROR.42") //$NON-NLS-1$
            field = value
        }
    internal val whenConditions = mutableListOf<SimpleWhenCondition<T>>()

    fun `when`(condition: VisitableCondition<T>, vararg conditions: VisitableCondition<T>) =
        SimpleCaseThenGatherer(condition, conditions.asList())

    fun `else`(value: String) {
        this.elseValue = value
    }

    inner class SimpleCaseThenGatherer(val condition: VisitableCondition<T>,
                                       val conditions: List<VisitableCondition<T>>) {
        fun then(value: String) {
            val allConditions = buildList {
                add(condition)
                addAll(conditions)
            }

            whenConditions.add(SimpleWhenCondition(allConditions, value))
        }
    }
}
