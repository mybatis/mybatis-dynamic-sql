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
package org.mybatis.dynamic.sql.select;

import static org.mybatis.dynamic.sql.util.StringUtilities.quoteStringForSQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;

public class SearchedCaseDSL {
    private final List<SearchedCaseModel.SearchedWhenCondition> whenConditions = new ArrayList<>();
    private Object elseValue;

    public <T> WhenDSL when(BindableColumn<T> column, VisitableCondition<T> condition,
                            AndOrCriteriaGroup... subCriteria) {
        return when(column, condition, Arrays.asList(subCriteria));
    }

    public <T> WhenDSL when(BindableColumn<T> column, VisitableCondition<T> condition,
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
    public SearchedCaseEnder else_(String value) {
        this.elseValue = quoteStringForSQL(value);
        return new SearchedCaseEnder();
    }

    @SuppressWarnings("java:S100")
    public SearchedCaseEnder else_(Object value) {
        this.elseValue = value;
        return new SearchedCaseEnder();
    }

    public BasicColumn end() {
        return new SearchedCaseModel.Builder()
                .withElseValue(elseValue)
                .withWhenConditions(whenConditions)
                .build();
    }

    public class WhenDSL extends AbstractBooleanExpressionDSL<WhenDSL> {
        private WhenDSL(SqlCriterion sqlCriterion) {
            setInitialCriterion(sqlCriterion);
        }

        public SearchedCaseDSL then(String value) {
            whenConditions.add(new SearchedCaseModel.SearchedWhenCondition(getInitialCriterion(), subCriteria,
                    quoteStringForSQL(value)));
            return SearchedCaseDSL.this;
        }

        public SearchedCaseDSL then(Object value) {
            whenConditions.add(new SearchedCaseModel.SearchedWhenCondition(getInitialCriterion(), subCriteria, value));
            return SearchedCaseDSL.this;
        }

        @Override
        protected WhenDSL getThis() {
            return this;
        }
    }

    public class SearchedCaseEnder {
        public BasicColumn end() {
            return SearchedCaseDSL.this.end();
        }
    }

    public static SearchedCaseDSL searchedCase() {
        return new SearchedCaseDSL();
    }
}
