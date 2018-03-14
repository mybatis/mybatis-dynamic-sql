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
package org.mybatis.dynamic.sql.insert.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultInsertSelectStatementProvider implements InsertSelectStatementProvider {
    private String tableName;
    private Optional<String> columnsPhrase;
    private String selectStatement;
    private Map<String, Object> parameters;
    
    private DefaultInsertSelectStatementProvider(Builder builder) {
        tableName = Objects.requireNonNull(builder.tableName);
        columnsPhrase = Objects.requireNonNull(builder.columnsPhrase);
        selectStatement = Objects.requireNonNull(builder.selectStatement);
        parameters = Objects.requireNonNull(builder.parameters);
    }
    
    @Override
    public String getInsertStatement() {
        return "insert into" //$NON-NLS-1$
                + spaceBefore(tableName)
                + spaceBefore(columnsPhrase)
                + spaceBefore(selectStatement);
    }
    
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static Builder withTableName(String tableName) {
        return new Builder().withTableName(tableName);
    }
    
    public static class Builder {
        private String tableName;
        private Optional<String> columnsPhrase;
        private String selectStatement;
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder withColumnsPhrase(Optional<String> columnsPhrase) {
            this.columnsPhrase = columnsPhrase;
            return this;
        }
        
        public Builder withSelectStatement(String selectStatement) {
            this.selectStatement = selectStatement;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public DefaultInsertSelectStatementProvider build() {
            return new DefaultInsertSelectStatementProvider(this);
        }
    }
}
