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

public abstract class AbstractHavingStarter<F extends AbstractHavingFinisher<?>> {

    public <T> F having(BindableColumn<T> column, VisitableCondition<T> condition,
                        AndOrCriteriaGroup... subCriteria) {
        return having(column, condition, Arrays.asList(subCriteria));
    }

    public <T> F having(BindableColumn<T> column, VisitableCondition<T> condition,
                        List<AndOrCriteriaGroup> subCriteria) {
        SqlCriterion sqlCriterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    public F having(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return having(initialCriterion, Arrays.asList(subCriteria));
    }

    public F having(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        SqlCriterion sqlCriterion = new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    protected abstract F having();

    public F applyHaving(HavingApplier havingApplier) {
        F finisher = having();
        havingApplier.accept(finisher);
        return finisher;
    }

    private F initialize(SqlCriterion sqlCriterion) {
        F finisher = having();
        finisher.initialize(sqlCriterion);
        return finisher;
    }
}
