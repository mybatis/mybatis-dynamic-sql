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
package org.mybatis.dynamic.sql;

import java.util.Objects;

public class ColumnAndConditionCriterion<T> extends SqlCriterion {
    private final BindableColumn<T> column;
    private final VisitableCondition<T> condition;

    private ColumnAndConditionCriterion(Builder<T> builder) {
        super(builder);
        column = Objects.requireNonNull(builder.column);
        condition = Objects.requireNonNull(builder.condition);
    }

    public BindableColumn<T> column() {
        return column;
    }

    public VisitableCondition<T> condition() {
        return condition;
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static <T> Builder<T> withColumn(BindableColumn<T> column) {
        return new Builder<T>().withColumn(column);
    }

    public static class Builder<T> extends AbstractBuilder<Builder<T>> {
        private BindableColumn<T> column;
        private VisitableCondition<T> condition;

        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }

        public Builder<T> withCondition(VisitableCondition<T> condition) {
            this.condition = condition;
            return this;
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }

        public ColumnAndConditionCriterion<T> build() {
            return new ColumnAndConditionCriterion<>(this);
        }
    }
}
