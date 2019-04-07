/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.delete.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultDeleteStatementProvider implements DeleteStatementProvider {
    private String deleteStatement;
    private Map<String, Object> parameters;
    
    private DefaultDeleteStatementProvider(Builder builder) {
        deleteStatement = Objects.requireNonNull(builder.deleteStatement);
        parameters = Objects.requireNonNull(builder.parameters);
    }
    
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    @Override
    public String getDeleteStatement() {
        return deleteStatement;
    }

    public static Builder withDeleteStatement(String deleteStatement) {
        return new Builder().withDeleteStatement(deleteStatement);
    }
    
    public static class Builder {
        private String deleteStatement;
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withDeleteStatement(String deleteStatement) {
            this.deleteStatement = deleteStatement;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public DefaultDeleteStatementProvider build() {
            return new DefaultDeleteStatementProvider(this);
        }
    }
}