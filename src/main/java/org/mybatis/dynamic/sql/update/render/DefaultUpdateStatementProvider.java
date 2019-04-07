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
package org.mybatis.dynamic.sql.update.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultUpdateStatementProvider implements UpdateStatementProvider {
    private String updateStatement;
    private Map<String, Object> parameters = new HashMap<>();

    private DefaultUpdateStatementProvider(Builder builder) {
        updateStatement = Objects.requireNonNull(builder.updateStatement);
        parameters.putAll(builder.parameters);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String getUpdateStatement() {
        return updateStatement;
    }
    
    public static Builder withUpdateStatement(String updateStatement) {
        return new Builder().withUpdateStatement(updateStatement);
    }
    
    public static class Builder {
        private String updateStatement;
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withUpdateStatement(String updateStatement) {
            this.updateStatement = updateStatement;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public DefaultUpdateStatementProvider build() {
            return new DefaultUpdateStatementProvider(this);
        }
    }
}
