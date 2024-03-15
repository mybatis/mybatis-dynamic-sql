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
package org.mybatis.dynamic.sql.select.caseexpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.render.SearchedCaseRenderer;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.Validator;

public class SearchedCaseModel implements BasicColumn {
    private final List<SearchedWhenCondition> whenConditions;
    private final BasicColumn elseValue;
    private final String alias;

    private SearchedCaseModel(Builder builder) {
        whenConditions = builder.whenConditions;
        alias = builder.alias;
        elseValue = builder.elseValue;
        Validator.assertNotEmpty(whenConditions, "ERROR.40"); //$NON-NLS-1$
    }

    public Stream<SearchedWhenCondition> whenConditions() {
        return whenConditions.stream();
    }

    public Optional<BasicColumn> elseValue() {
        return Optional.ofNullable(elseValue);
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public SearchedCaseModel as(String alias) {
        return new Builder().withWhenConditions(whenConditions)
                .withElseValue(elseValue)
                .withAlias(alias)
                .build();
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return new SearchedCaseRenderer(this, renderingContext).render();
    }

    public static class SearchedWhenCondition extends AbstractBooleanExpressionModel {

        private final BasicColumn thenValue;

        public BasicColumn thenValue() {
            return thenValue;
        }

        public SearchedWhenCondition(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria,
                                     BasicColumn thenValue) {
            super(initialCriterion, subCriteria);
            this.thenValue = Objects.requireNonNull(thenValue);
        }
    }

    public static class Builder {
        private final List<SearchedWhenCondition> whenConditions = new ArrayList<>();
        private BasicColumn elseValue;
        private String alias;

        public Builder withWhenConditions(List<SearchedWhenCondition> whenConditions) {
            this.whenConditions.addAll(whenConditions);
            return this;
        }

        public Builder withElseValue(BasicColumn elseValue) {
            this.elseValue = elseValue;
            return this;
        }

        public Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public SearchedCaseModel build() {
            return new SearchedCaseModel(this);
        }
    }
}
