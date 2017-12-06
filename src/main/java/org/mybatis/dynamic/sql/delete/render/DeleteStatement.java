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
package org.mybatis.dynamic.sql.delete.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.where.render.WhereClauseAndParameters;

public class DeleteStatement {
    private String tableName;
    private Optional<WhereClauseAndParameters> whereClauseAndParameters;
    
    private DeleteStatement(Builder builder) {
        tableName = Objects.requireNonNull(builder.tableName);
        whereClauseAndParameters = Objects.requireNonNull(builder.whereClauseAndParameters);
    }
    
    public Map<String, Object> getParameters() {
        return whereClauseAndParameters.map(WhereClauseAndParameters::parameters)
                .orElse(Collections.emptyMap());
    }
    
    public String getDeleteStatement() {
        return "delete from" //$NON-NLS-1$
                + spaceBefore(tableName)
                + spaceBefore(whereClauseAndParameters.map(WhereClauseAndParameters::whereClause));
    }

    public static class Builder {
        private String tableName;
        private Optional<WhereClauseAndParameters> whereClauseAndParameters = Optional.empty();
        
        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder withWhereClause(Optional<WhereClauseAndParameters> whereClauseAndParameters) {
            this.whereClauseAndParameters = whereClauseAndParameters;
            return this;
        }
        
        public DeleteStatement build() {
            return new DeleteStatement(this);
        }
    }
}