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
package org.mybatis.dynamic.sql.update.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

/**
 * This class combines a "set" clause and a "where" clause into one parameter object
 * that can be sent to a MyBatis3 mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class DefaultUpdateStatementProvider implements UpdateStatementProvider {
    private String tableName;
    private String setClause;
    private Optional<String> whereClause;
    private Map<String, Object> parameters = new HashMap<>();

    private DefaultUpdateStatementProvider(Builder builder) {
        tableName = Objects.requireNonNull(builder.tableName);
        setClause = Objects.requireNonNull(builder.setClause);
        whereClause = Optional.ofNullable(builder.whereClause);
        parameters.putAll(builder.parameters);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String getUpdateStatement() {
        return "update" //$NON-NLS-1$
                + spaceBefore(tableName)
                + spaceBefore(setClause)
                + spaceBefore(whereClause);
    }
    
    public static Builder withTableName(String tableName) {
        return new Builder().withTableName(tableName);
    }
    
    public static class Builder {
        private String tableName;
        private String setClause;
        private String whereClause;
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder withSetClause(String setClause) {
            this.setClause = setClause;
            return this;
        }
        
        public Builder withWhereClause(Optional<WhereClauseProvider> whereClauseProvider) {
            whereClauseProvider.ifPresent(wcp -> {
                whereClause = wcp.getWhereClause();
                parameters.putAll(wcp.getParameters());
            });
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
