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
package org.mybatis.dynamic.sql.where.render;

import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class ColumnAndConditionRenderer<T> {
    private final BindableColumn<T> column;
    private final VisitableCondition<T> condition;
    private final RenderingContext renderingContext;

    private ColumnAndConditionRenderer(Builder<T> builder) {
        column = Objects.requireNonNull(builder.column);
        condition = Objects.requireNonNull(builder.condition);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
    }

    public FragmentAndParameters render() {
        FragmentCollector fc = new FragmentCollector();
        fc.add(condition.renderLeftColumn(renderingContext, column));
        fc.add(condition.renderCondition(renderingContext, column));
        return fc.toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    public static class Builder<T> {
        private @Nullable BindableColumn<T> column;
        private @Nullable VisitableCondition<T> condition;
        private @Nullable RenderingContext renderingContext;

        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }

        public Builder<T> withCondition(VisitableCondition<T> condition) {
            this.condition = condition;
            return this;
        }

        public Builder<T> withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public ColumnAndConditionRenderer<T> build() {
            return new ColumnAndConditionRenderer<>(this);
        }
    }
}
