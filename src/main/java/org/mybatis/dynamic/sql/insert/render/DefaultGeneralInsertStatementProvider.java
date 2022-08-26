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
package org.mybatis.dynamic.sql.insert.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultGeneralInsertStatementProvider
        implements GeneralInsertStatementProvider, InsertSelectStatementProvider {
    private final String insertStatement;
    private final Map<String, Object> parameters = new HashMap<>();

    private DefaultGeneralInsertStatementProvider(Builder builder) {
        insertStatement = Objects.requireNonNull(builder.insertStatement);
        parameters.putAll(builder.parameters);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String getInsertStatement() {
        return insertStatement;
    }

    public static Builder withInsertStatement(String insertStatement) {
        return new Builder().withInsertStatement(insertStatement);
    }

    public static class Builder {
        private String insertStatement;
        private final Map<String, Object> parameters = new HashMap<>();

        public Builder withInsertStatement(String insertStatement) {
            this.insertStatement = insertStatement;
            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public DefaultGeneralInsertStatementProvider build() {
            return new DefaultGeneralInsertStatementProvider(this);
        }
    }
}
