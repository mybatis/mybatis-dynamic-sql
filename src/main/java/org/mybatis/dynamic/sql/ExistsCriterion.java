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

public class ExistsCriterion extends SqlCriterion {
    private final ExistsPredicate existsPredicate;

    private ExistsCriterion(Builder builder) {
        super(builder);
        this.existsPredicate = Objects.requireNonNull(builder.existsPredicate);
    }

    public ExistsPredicate existsPredicate() {
        return existsPredicate;
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private ExistsPredicate existsPredicate;

        public Builder withExistsPredicate(ExistsPredicate existsPredicate) {
            this.existsPredicate = existsPredicate;
            return this;
        }

        public ExistsCriterion build() {
            return new ExistsCriterion(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
