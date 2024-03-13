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

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.render.SimpleCaseRenderer;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.Validator;

public class SimpleCaseModel<T> implements BasicColumn {
    private final BindableColumn<T> column;
    private final List<SimpleCaseWhenCondition<T>> whenConditions;
    private final Object elseValue;
    private final String alias;

    private SimpleCaseModel(Builder<T> builder) {
        column = Objects.requireNonNull(builder.column);
        whenConditions = builder.whenConditions;
        elseValue = builder.elseValue;
        alias = builder.alias;
        Validator.assertNotEmpty(whenConditions, "ERROR.40"); //$NON-NLS-1$
    }

    public BindableColumn<T> column() {
        return column;
    }

    public Stream<SimpleCaseWhenCondition<T>> whenConditions() {
        return whenConditions.stream();
    }

    public Optional<Object> elseValue() {
        return Optional.ofNullable(elseValue);
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public SimpleCaseModel<T> as(String alias) {
        return new Builder<T>()
                .withColumn(column)
                .withWhenConditions(whenConditions)
                .withElseValue(elseValue)
                .withAlias(alias)
                .build();
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return new SimpleCaseRenderer<>(this, renderingContext).render();
    }

    public static class Builder<T> {
        private BindableColumn<T> column;
        private final List<SimpleCaseWhenCondition<T>> whenConditions = new ArrayList<>();
        private Object elseValue;
        private String alias;

        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }

        public Builder<T> withWhenConditions(List<SimpleCaseWhenCondition<T>> whenConditions) {
            this.whenConditions.addAll(whenConditions);
            return this;
        }

        public Builder<T> withElseValue(Object elseValue) {
            this.elseValue = elseValue;
            return this;
        }

        public Builder<T> withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public SimpleCaseModel<T> build() {
            return new SimpleCaseModel<>(this);
        }
    }
}
