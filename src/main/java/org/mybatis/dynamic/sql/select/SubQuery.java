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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.TableExpressionVisitor;

public class SubQuery implements TableExpression {
    private final SelectModel selectModel;
    private final String alias;

    private SubQuery(Builder builder) {
        selectModel = Objects.requireNonNull(builder.selectModel);
        alias = builder.alias;
    }

    public SelectModel selectModel() {
        return selectModel;
    }

    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public boolean isSubQuery() {
        return true;
    }

    @Override
    public <R> R accept(TableExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static class Builder {
        private SelectModel selectModel;
        private String alias;

        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }

        public Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public SubQuery build() {
            return new SubQuery(this);
        }
    }
}
