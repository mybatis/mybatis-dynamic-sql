/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;

public abstract class WhereOrHavingApplier<T extends WhereOrHavingApplier<T>> {
    private final SqlCriterion initialCriterion;
    private final List<AndOrCriteriaGroup> subCriteria =  new ArrayList<>();
    private final Consumer<BooleanOperations<?>> after;

    protected WhereOrHavingApplier(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        this.initialCriterion = initialCriterion;
        this.subCriteria.addAll(subCriteria);
        after = scc -> { };
    }

    protected WhereOrHavingApplier(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria,
                                 Consumer<BooleanOperations<?>> after) {
        this.initialCriterion = initialCriterion;
        this.subCriteria.addAll(subCriteria);
        this.after = after;
    }

    public SqlCriterion initialCriterion() {
        return initialCriterion;
    }

    public List<AndOrCriteriaGroup> subCriteria() {
        var scc = new SubCriteriaCollector();
        after.accept(scc);
        List<AndOrCriteriaGroup> answer = new ArrayList<>();
        answer.addAll(subCriteria);
        answer.addAll(scc.subCriteria());
        return answer;
    }

    /**
     * Return a composed applier that performs this operation followed by the after operation.
     *
     * @param after the operation to perform after this operation
     *
     * @return a composed applier that performs this operation followed by the after operation.
     */
    public T andThen(Consumer<BooleanOperations<?>> after) {
        var newConsumer = this.after.andThen(after);
        return buildNew(initialCriterion, subCriteria, newConsumer);
    }

    protected abstract T buildNew(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria,
                                  Consumer<BooleanOperations<?>> after);

    private static class SubCriteriaCollector implements BooleanOperations<SubCriteriaCollector> {
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        @Override
        public SubCriteriaCollector addSubCriterion(AndOrCriteriaGroup subCriterion) {
            subCriteria.add(subCriterion);
            return this;
        }

        public List<AndOrCriteriaGroup> subCriteria() {
            return subCriteria;
        }
    }
}
