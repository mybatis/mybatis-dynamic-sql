/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.select.caseexpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.RenderableCondition;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;

public class SearchedCaseDSL implements ElseDSL<SearchedCaseDSL.SearchedCaseEnder> {
    private final List<SearchedCaseWhenCondition> whenConditions = new ArrayList<>();
    private @Nullable BasicColumn elseValue;

    public <T> WhenDSL when(BindableColumn<T> column, RenderableCondition<T> condition,
                            AndOrCriteriaGroup... subCriteria) {
        return when(column, condition, Arrays.asList(subCriteria));
    }

    public <T> WhenDSL when(BindableColumn<T> column, RenderableCondition<T> condition,
                            List<AndOrCriteriaGroup> subCriteria) {
        SqlCriterion sqlCriterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    public WhenDSL when(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return when(initialCriterion, Arrays.asList(subCriteria));
    }

    public WhenDSL when(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        SqlCriterion sqlCriterion = new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    private WhenDSL initialize(SqlCriterion sqlCriterion) {
        return new WhenDSL(sqlCriterion);
    }

    @SuppressWarnings("java:S100")
    @Override
    public SearchedCaseEnder else_(BasicColumn column) {
        elseValue = column;
        return new SearchedCaseEnder();
    }

    public SearchedCaseModel end() {
        return new SearchedCaseModel.Builder()
                .withElseValue(elseValue)
                .withWhenConditions(whenConditions)
                .build();
    }

    public class WhenDSL extends AbstractBooleanExpressionDSL<WhenDSL> implements ThenDSL<SearchedCaseDSL> {
        private WhenDSL(SqlCriterion sqlCriterion) {
            setInitialCriterion(sqlCriterion);
        }

        @Override
        public SearchedCaseDSL then(BasicColumn column) {
            whenConditions.add(new SearchedCaseWhenCondition.Builder()
                    .withInitialCriterion(getInitialCriterion())
                    .withSubCriteria(subCriteria)
                    .withThenValue(column)
                    .build());
            return SearchedCaseDSL.this;
        }

        @Override
        protected WhenDSL getThis() {
            return this;
        }
    }

    public class SearchedCaseEnder {
        public SearchedCaseModel end() {
            return SearchedCaseDSL.this.end();
        }
    }

    public static SearchedCaseDSL searchedCase() {
        return new SearchedCaseDSL();
    }
}
