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

import java.util.Objects;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel;

public class SearchedCaseWhenCondition extends AbstractBooleanExpressionModel {
    private final BasicColumn thenValue;

    public BasicColumn thenValue() {
        return thenValue;
    }

    private SearchedCaseWhenCondition(Builder builder) {
        super(builder);
        thenValue = Objects.requireNonNull(builder.thenValue);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private BasicColumn thenValue;

        public Builder withThenValue(BasicColumn thenValue) {
            this.thenValue = thenValue;
            return this;
        }

        public SearchedCaseWhenCondition build() {
            return new SearchedCaseWhenCondition(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
