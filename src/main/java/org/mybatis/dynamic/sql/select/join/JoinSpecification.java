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
package org.mybatis.dynamic.sql.select.join;

import java.util.Objects;

import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel;
import org.mybatis.dynamic.sql.util.Validator;

public class JoinSpecification extends AbstractBooleanExpressionModel {

    private final TableExpression table;
    private final JoinType joinType;

    private JoinSpecification(Builder builder) {
        super(builder);
        table = Objects.requireNonNull(builder.table);
        joinType = Objects.requireNonNull(builder.joinType);
        Validator.assertFalse(initialCriterion().isEmpty() && subCriteria().isEmpty(),
                "ERROR.16"); //$NON-NLS-1$
    }

    public TableExpression table() {
        return table;
    }

    public JoinType joinType() {
        return joinType;
    }

    public static Builder withJoinTable(TableExpression table) {
        return new Builder().withJoinTable(table);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private TableExpression table;
        private JoinType joinType;

        public Builder withJoinTable(TableExpression table) {
            this.table = table;
            return this;
        }

        public Builder withJoinType(JoinType joinType) {
            this.joinType = joinType;
            return this;
        }

        public JoinSpecification build() {
            return new JoinSpecification(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
