/*
 *    Copyright 2016-2026 the original author or authors.
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

/**
 * A simple SqlCriterion that does nothing and will not render. This is to solve the problem of
 * awkward nullability operations in various classes. We can now say that an initial criterion is never null.
 */
public class NullCriterion extends SqlCriterion {
    public NullCriterion() {
        super(new Builder());
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    private static class Builder extends AbstractBuilder<Builder> {
        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
