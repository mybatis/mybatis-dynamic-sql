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

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;
import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

public class QueryExpression {
    
    private String tableName;
    private Optional<String> connector;
    private String columnList;
    private Optional<String> whereClause;
    private Map<String, Object> parameters;
    private boolean isDistinct;
    private Optional<String> joinClause;
    private Optional<String> groupByClause;
    
    private QueryExpression(Builder builder) {
        tableName = Objects.requireNonNull(builder.tableName);
        connector = Objects.requireNonNull(builder.connector);
        columnList = Objects.requireNonNull(builder.columnList);
        whereClause = Optional.ofNullable(builder.whereClause);
        parameters = Objects.requireNonNull(builder.parameters);
        isDistinct = builder.isDistinct;
        joinClause = Objects.requireNonNull(builder.joinClause);
        groupByClause = Objects.requireNonNull(builder.groupByClause);
    }
    
    public Map<String, Object> parameters() {
        return parameters;
    }
    
    public String queryExpression() {
        return spaceAfter(connector)
                + "select " //$NON-NLS-1$
                + (isDistinct ? "distinct " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + columnList
                + " from " //$NON-NLS-1$
                + tableName
                + spaceBefore(joinClause)
                + spaceBefore(whereClause)
                + spaceBefore(groupByClause);
    }
    
    public static Builder withColumnList(String columnList) {
        return new Builder().withColumnList(columnList);
    }
    
    public static class Builder {
        private Optional<String> connector;
        private String tableName;
        private boolean isDistinct;
        private String columnList;
        private Optional<String> joinClause = Optional.empty();
        private String whereClause;
        private Map<String, Object> parameters = new HashMap<>();
        private Optional<String> groupByClause = Optional.empty();
        
        public Builder withConnector(Optional<String> connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder isDistinct(boolean isDistinct) {
            this.isDistinct = isDistinct;
            return this;
        }
        
        public Builder withWhereClause(Optional<WhereClauseProvider> whereClauseProvider) {
            whereClauseProvider.ifPresent(wcp -> {
                whereClause = wcp.getWhereClause();
                parameters.putAll(wcp.getParameters());
            });
            return this;
        }
        
        public Builder withColumnList(String columnList) {
            this.columnList = columnList;
            return this;
        }
        
        public Builder withJoinClause(Optional<String> joinClause) {
            this.joinClause = joinClause;
            return this;
        }
        
        public Builder withGroupByClause(Optional<String> groupByClause) {
            this.groupByClause = groupByClause;
            return this;
        }
        
        public QueryExpression build() {
            return new QueryExpression(this);
        }
    }
}
