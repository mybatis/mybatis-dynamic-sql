/*
 *    Copyright 2016-2022 the original author or authors.
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
import java.util.Collections;
import java.util.List;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;

public class HavingDSL extends AbstractBooleanExpressionDSL<HavingDSL> {
    public <T> HavingDSL having(BindableColumn<T> column, VisitableCondition<T> condition,
                                AndOrCriteriaGroup... subCriteria) {
        return having(column, condition, Arrays.asList(subCriteria));
    }

    public <T> HavingDSL having(BindableColumn<T> column, VisitableCondition<T> condition,
                                List<AndOrCriteriaGroup> subCriteria) {
        return having(ColumnAndConditionCriterion.withColumn(column).withCondition(condition).build(), subCriteria);
    }

    public HavingDSL having(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return having(initialCriterion, Arrays.asList(subCriteria));
    }

    public HavingDSL having(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        setInitialCriterion(initialCriterion);
        this.subCriteria.addAll(subCriteria);
        return this;
    }

    @Override
    protected HavingDSL getThis() {
        return this;
    }

    @Override
    protected SqlCriterion getInitialCriterion() {
        return super.getInitialCriterion();
    }

    protected List<AndOrCriteriaGroup> getSubCriteria() {
        return Collections.unmodifiableList(subCriteria);
    }
}
