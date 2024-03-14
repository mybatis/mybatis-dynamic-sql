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
import org.mybatis.dynamic.sql.select.caseexpression.BasicWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.ConditionBasedWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseModel.SearchedWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseWhenCondition
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaCollector
import org.mybatis.dynamic.sql.util.kotlin.assertNull

class KSearchedCaseDSL {
    internal var elseValue: Any? = null
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
        this.elseValue = "'$value'"
    }

    fun `else`(value: Any) {
        this.elseValue = value
    }
}

class SearchedCaseCriteriaCollector : GroupingCriteriaCollector() {
    internal var thenValue: Any? = null
        private set(value) {
            assertNull(field, "ERROR.41") //$NON-NLS-1$
            field = value
        }

    fun then(value: String) {
        thenValue = "'$value'"
    }

    fun then(value: Any) {
        thenValue = value
    }
}

class KSimpleCaseDSL<T : Any> {
    internal var elseValue: Any? = null
        private set(value) {
            assertNull(field, "ERROR.42") //$NON-NLS-1$
            field = value
        }
    internal val whenConditions = mutableListOf<SimpleCaseWhenCondition<T>>()

    fun `when`(firstCondition: VisitableCondition<T>, vararg subsequentConditions: VisitableCondition<T>,
               completer: SimpleCaseThenGatherer.() -> Unit) =
        SimpleCaseThenGatherer().apply(completer).run {
            val allConditions = buildList {
                add(firstCondition)
                addAll(subsequentConditions)
            }

            whenConditions.add(ConditionBasedWhenCondition(allConditions, thenValue))
        }

    fun `when`(firstValue: T, vararg subsequentValues: T, completer: SimpleCaseThenGatherer.() -> Unit) =
        SimpleCaseThenGatherer().apply(completer).run {
            val allConditions = buildList {
                add(firstValue)
                addAll(subsequentValues)
            }

            whenConditions.add(BasicWhenCondition(allConditions, thenValue))
        }

    fun `else`(value: String) {
        this.elseValue = "'$value'"
    }

    fun `else`(value: Any) {
        this.elseValue = value
    }
}

class SimpleCaseThenGatherer {
    internal var thenValue: Any? = null
        private set(value) {
            assertNull(field, "ERROR.41") //$NON-NLS-1$
            field = value
        }

    fun then(value: String) {
        thenValue = "'$value'"
    }

    fun then(value: Any) {
        thenValue = value
    }
}
