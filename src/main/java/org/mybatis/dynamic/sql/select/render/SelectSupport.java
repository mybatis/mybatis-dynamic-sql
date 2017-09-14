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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mybatis.dynamic.sql.AbstractSqlSupport;

public class SelectSupport extends AbstractSqlSupport {
    
    private static final String DISTINCT_STRING = "distinct"; //$NON-NLS-1$

    private String columnList;
    private Optional<String> whereClause;
    private Map<String, Object> parameters = new HashMap<>();
    private Optional<String> distinct;
    private Optional<String> orderByClause;
    
    private SelectSupport(Builder builder) {
        super(builder.tableName);
        columnList = builder.columnList;
        whereClause = Optional.ofNullable(builder.whereClause);
        parameters.putAll(builder.parameters);
        distinct = Optional.ofNullable(builder.distinct);
        orderByClause = builder.orderByClause;
    }
    
    public String getDistinct() {
        return distinct().orElse(EMPTY_STRING);
    }
    
    private Optional<String> distinct() {
        return distinct;
    }
    
    public String getWhereClause() {
        return whereClause.orElse(EMPTY_STRING);
    }

    public Optional<String> whereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getOrderByClause() {
        return orderByClause.orElse(EMPTY_STRING);
    }
    
    public Optional<String> orderByClause() {
        return orderByClause;
    }
    
    public String getColumnList() {
        return columnList;
    }
    
    public String getFullSelectStatement() {
        return "select " //$NON-NLS-1$
                + distinct().map(d -> d + ONE_SPACE).orElse(EMPTY_STRING)
                + getColumnList()
                + " from " //$NON-NLS-1$
                + tableName()
                + whereClause().map(w -> ONE_SPACE + w).orElse(EMPTY_STRING)
                + orderByClause().map(o -> ONE_SPACE + o).orElse(EMPTY_STRING);
    }
    
    public static class Builder {
        private String tableName;
        private String distinct;
        private Optional<String> orderByClause = Optional.empty();
        private String whereClause;
        private Map<String, Object> parameters = new HashMap<>();
        private String columnList;
        
        public Builder(String tableName) {
            this.tableName = tableName;
        }
        
        public Builder isDistinct(boolean isDistinct) {
            distinct = isDistinct ? DISTINCT_STRING : null;
            return this;
        }
        
        public Builder withOrderByClause(Optional<String> orderByClause) {
            this.orderByClause = orderByClause;
            return this;
        }
        
        public Builder withWhereClause(String whereClause) {
            this.whereClause = whereClause;
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
        
        public SelectSupport build() {
            return new SelectSupport(this);
        }
    }
}
