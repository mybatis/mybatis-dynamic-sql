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
package org.mybatis.dynamic.sql.select.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;
import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.AbstractSqlSupport;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class RenderedQueryExpression extends AbstractSqlSupport {
    
    private Optional<String> connector;
    private String columnList;
    private Optional<String> whereClause;
    private Map<String, Object> parameters;
    private boolean isDistinct;
    private Optional<String> joinClause;
    private Optional<String> groupByClause;
    
    private RenderedQueryExpression(Builder builder) {
        super(builder.tableName);
        connector = Objects.requireNonNull(builder.connector);
        columnList = Objects.requireNonNull(builder.columnList);
        whereClause = Objects.requireNonNull(builder.whereClause);
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
                + tableName()
                + spaceBefore(joinClause)
                + spaceBefore(whereClause)
                + spaceBefore(groupByClause);
    }
    
    public static class Builder {
        private Optional<String> connector;
        private String tableName;
        private boolean isDistinct;
        private Map<String, Object> parameters = new HashMap<>();
        private String columnList;
        private Optional<String> joinClause = Optional.empty();
        private Optional<String> whereClause = Optional.empty();
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
        
        public Builder withWhereSupport(Optional<WhereSupport> whereSupport) {
            whereClause = whereSupport.map(WhereSupport::getWhereClause);
            parameters.putAll(whereSupport.map(WhereSupport::getParameters).orElse(Collections.emptyMap()));
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
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
        
        public RenderedQueryExpression build() {
            return new RenderedQueryExpression(this);
        }
    }
}
