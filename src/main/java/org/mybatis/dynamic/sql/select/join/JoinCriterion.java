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
package org.mybatis.dynamic.sql.select.join;

import java.util.Objects;

import org.mybatis.dynamic.sql.BindableColumn;

public class JoinCriterion<T> {

    private final String connector;
    private final BindableColumn<T> leftColumn;
    private final JoinCondition<T> joinCondition;

    private JoinCriterion(Builder<T> builder) {
        connector = Objects.requireNonNull(builder.connector);
        leftColumn = Objects.requireNonNull(builder.joinColumn);
        joinCondition = Objects.requireNonNull(builder.joinCondition);
    }

    public String connector() {
        return connector;
    }

    public BindableColumn<T> leftColumn() {
        return leftColumn;
    }

    public JoinCondition<T> joinCondition() {
        return joinCondition;
    }

    public static class Builder<T> {
        private String connector;
        private BindableColumn<T> joinColumn;
        private JoinCondition<T> joinCondition;

        public Builder<T> withConnector(String connector) {
            this.connector = connector;
            return this;
        }

        public Builder<T> withJoinColumn(BindableColumn<T> joinColumn) {
            this.joinColumn = joinColumn;
            return this;
        }

        public Builder<T> withJoinCondition(JoinCondition<T> joinCondition) {
            this.joinCondition = joinCondition;
            return this;
        }

        public JoinCriterion<T> build() {
            return new JoinCriterion<>(this);
        }
    }
}
