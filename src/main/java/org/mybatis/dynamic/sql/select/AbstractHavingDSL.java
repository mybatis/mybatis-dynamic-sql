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

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;

public abstract class AbstractHavingDSL<T extends AbstractHavingDSL<T>> extends AbstractBooleanExpressionDSL<T> {
    public <S> T having(BindableColumn<S> column, VisitableCondition<S> condition,
                                        AndOrCriteriaGroup... subCriteria) {
        return having(column, condition, Arrays.asList(subCriteria));
    }

    public <S> T having(BindableColumn<S> column, VisitableCondition<S> condition,
                                        List<AndOrCriteriaGroup> subCriteria) {
        setInitialCriterion(ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build(), "ERROR.31"); //$NON-NLS-1$
        return getThis();
    }

    public T having(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return having(initialCriterion, Arrays.asList(subCriteria));
    }

    public T having(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        setInitialCriterion(new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build(), "ERROR.31"); //$NON-NLS-1$
        return getThis();
    }

    public T applyHaving(HavingApplier havingApplier) {
        havingApplier.accept(this);
        return getThis();
    }

    @Override
    protected abstract T getThis();
}
