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

import java.util.Optional;

/**
 * This class represents a criteria group without an AND or an OR connector. This is useful
 * in situations where the initial SqlCriterion in a list should be further grouped
 * as in an expression like ((A &lt; 5 and B &gt; 6) or C = 3)
 *
 * @author Jeff Butler, inspired by @JoshuaJeme
 * @since 1.4.0
 */
public class CriteriaGroup extends SqlCriterion {
    private final SqlCriterion initialCriterion;

    protected CriteriaGroup(AbstractGroupBuilder<?> builder) {
        super(builder);
        initialCriterion = builder.initialCriterion;
    }

    public Optional<SqlCriterion> initialCriterion() {
        return Optional.ofNullable(initialCriterion);
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public abstract static class AbstractGroupBuilder<T extends AbstractGroupBuilder<T>> extends AbstractBuilder<T> {
        private SqlCriterion initialCriterion;

        public T withInitialCriterion(SqlCriterion initialCriterion) {
            this.initialCriterion = initialCriterion;
            return getThis();
        }
    }

    public static class Builder extends AbstractGroupBuilder<Builder> {
        public CriteriaGroup build() {
            return new CriteriaGroup(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
