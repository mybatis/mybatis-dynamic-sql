/**
 *    Copyright 2016-2017 the original author or authors.
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

import org.mybatis.dynamic.sql.AbstractSqlSupport;

public class InsertSelectSupport extends AbstractSqlSupport {
    
    private Optional<String> columnsPhrase;
    private String renderedSelectStatement;
    private Map<String, Object> parameters;
    
    private InsertSelectSupport(Builder builder) {
        super(builder.tableName);
        this.columnsPhrase = Objects.requireNonNull(builder.columnsPhrase);
        this.renderedSelectStatement = Objects.requireNonNull(builder.renderedSelectStatement);
        this.parameters = Objects.requireNonNull(builder.parameters);
    }
    
    public String getFullInsertStatement() {
        return "insert into" //$NON-NLS-1$
                + spaceBefore(tableName())
                + spaceBefore(columnsPhrase)
                + spaceBefore(renderedSelectStatement);
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static class Builder {
        private String tableName;
        private Optional<String> columnsPhrase;
        private String renderedSelectStatement;
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder withColumnsPhrase(Optional<String> columnsPhrase) {
            this.columnsPhrase = columnsPhrase;
            return this;
        }
        
        public Builder withSelectStatement(String renderedSelectStatement) {
            this.renderedSelectStatement = renderedSelectStatement;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public InsertSelectSupport build() {
            return new InsertSelectSupport(this);
        }
    }
}
