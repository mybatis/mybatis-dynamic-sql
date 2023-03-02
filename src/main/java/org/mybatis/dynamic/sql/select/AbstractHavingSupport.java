/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.select;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

public abstract class AbstractHavingSupport<W extends AbstractHavingDSL<?>> {
    public abstract W having();

    public <S> W having(BindableColumn<S> column, VisitableCondition<S> condition,
                        AndOrCriteriaGroup... subCriteria) {
        return having(column, condition, Arrays.asList(subCriteria));
    }

    public <S> W having(BindableColumn<S> column, VisitableCondition<S> condition,
                        List<AndOrCriteriaGroup> subCriteria) {
        return apply(w -> w.having(column, condition, subCriteria));
    }

    public W having(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return having(initialCriterion, Arrays.asList(subCriteria));
    }

    public W having(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        return apply(w -> w.having(initialCriterion, subCriteria));
    }

    public W applyHaving(HavingApplier havingApplier) {
        return apply(w -> w.applyHaving(havingApplier));
    }

    private W apply(Consumer<W> block) {
        W dsl = having();
        block.accept(dsl);
        return dsl;
    }
}
