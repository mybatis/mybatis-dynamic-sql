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
package org.mybatis.dynamic.sql.update.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.where.render.WhereClauseAndParameters;

/**
 * This class combines a "set" clause and a "where" clause into one parameter object
 * that can be sent to a MyBatis3 mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class UpdateStatement {
    private String tableName;
    private String setClause;
    private Optional<String> whereClause;
    private Map<String, Object> parameters;

    private UpdateStatement(Builder builder) {
        tableName = Objects.requireNonNull(builder.tableName);
        setClause = Objects.requireNonNull(builder.setClause);
        whereClause = Objects.requireNonNull(builder.whereClause);
        parameters = Objects.requireNonNull(builder.parameters);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getUpdateStatement() {
        return "update" //$NON-NLS-1$
                + spaceBefore(tableName)
                + spaceBefore(setClause)
                + spaceBefore(whereClause);
    }
    
    public static class Builder {
        private String tableName;
        private String setClause;
        private Optional<String> whereClause = Optional.empty();
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder withSetClause(String setClause) {
            this.setClause = setClause;
            return this;
        }
        
        public Builder withWhereClause(Optional<WhereClauseAndParameters> whereClauseAndParameters) {
            whereClause = whereClauseAndParameters.map(WhereClauseAndParameters::whereClause);
            parameters.putAll(whereClauseAndParameters.map(WhereClauseAndParameters::parameters)
                    .orElse(Collections.emptyMap()));
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public UpdateStatement build() {
            return new UpdateStatement(this);
        }
    }
}
