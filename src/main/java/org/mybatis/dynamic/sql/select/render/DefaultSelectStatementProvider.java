/**
 *    Copyright 2016-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.select.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultSelectStatementProvider implements SelectStatementProvider {
    private String queryExpression;
    private Map<String, Object> parameters;
    private Optional<String> orderByClause;
    
    private DefaultSelectStatementProvider(Builder builder) {
        queryExpression = Objects.requireNonNull(builder.queryExpression);
        orderByClause = Objects.requireNonNull(builder.orderByClause);
        parameters = Collections.unmodifiableMap(Objects.requireNonNull(builder.parameters));
    }
    
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    @Override
    public String getSelectStatement() {
        return queryExpression + spaceBefore(orderByClause);
    }
    
    public static Builder withQueryExpression(String queryExpression) {
        return new Builder().withQueryExpression(queryExpression);
    }
    
    public static class Builder {
        private String queryExpression;
        private Optional<String> orderByClause = Optional.empty();
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withQueryExpression(String queryExpression) {
            this.queryExpression = queryExpression;
            return this;
        }
        
        public Builder withOrderByClause(Optional<String> orderByClause) {
            this.orderByClause = orderByClause;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public DefaultSelectStatementProvider build() {
            return new DefaultSelectStatementProvider(this);
        }
    }
}
