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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.render.SearchedCaseRenderer;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.Validator;

public class SearchedCaseModel implements BasicColumn, SortSpecification {
    private final List<SearchedCaseWhenCondition> whenConditions;
    private final @Nullable BasicColumn elseValue;
    private final @Nullable String alias;
    private final String descendingPhrase;

    private SearchedCaseModel(Builder builder) {
        whenConditions = builder.whenConditions;
        alias = builder.alias;
        elseValue = builder.elseValue;
        descendingPhrase = builder.descendingPhrase;
        Validator.assertNotEmpty(whenConditions, "ERROR.40"); //$NON-NLS-1$
    }

    public Stream<SearchedCaseWhenCondition> whenConditions() {
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
                .withDescendingPhrase(descendingPhrase)
                .build();
    }

    @Override
    public SearchedCaseModel descending() {
        return new Builder().withWhenConditions(whenConditions)
                .withElseValue(elseValue)
                .withAlias(alias)
                .withDescendingPhrase(" DESC") //$NON-NLS-1$
                .build();
    }

    @Override
    public FragmentAndParameters renderForOrderBy(RenderingContext renderingContext) {
        return render(renderingContext).mapFragment(f -> f + descendingPhrase);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return new SearchedCaseRenderer(this, renderingContext).render();
    }

    public static class Builder {
        private final List<SearchedCaseWhenCondition> whenConditions = new ArrayList<>();
        private @Nullable BasicColumn elseValue;
        private @Nullable String alias;
        private String descendingPhrase = ""; //$NON-NLS-1$

        public Builder withWhenConditions(List<SearchedCaseWhenCondition> whenConditions) {
            this.whenConditions.addAll(whenConditions);
            return this;
        }

        public Builder withElseValue(@Nullable BasicColumn elseValue) {
            this.elseValue = elseValue;
            return this;
        }

        public Builder withAlias(@Nullable String alias) {
            this.alias = alias;
            return this;
        }

        public Builder withDescendingPhrase(String descendingPhrase) {
            this.descendingPhrase = descendingPhrase;
            return this;
        }

        public SearchedCaseModel build() {
            return new SearchedCaseModel(this);
        }
    }
}
