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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

public class DefaultWhereClauseProvider implements WhereClauseProvider {
    private final String whereClause;
    private final Map<String, Object> parameters;

    private DefaultWhereClauseProvider(Builder builder) {
        whereClause = Objects.requireNonNull(builder.whereClause);
        parameters = builder.parameters;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String getWhereClause() {
        return whereClause;
    }

    public static Builder withWhereClause(String whereClause) {
        return new Builder().withWhereClause(whereClause);
    }

    public static class Builder {
        private @Nullable String whereClause;
        private final Map<String, Object> parameters = new HashMap<>();

        public Builder withWhereClause(String whereClause) {
            this.whereClause = whereClause;
            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public DefaultWhereClauseProvider build() {
            return new DefaultWhereClauseProvider(this);
        }
    }
}
