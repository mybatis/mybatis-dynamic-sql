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
import org.mybatis.dynamic.sql.SqlTable;

public class SelectSupport extends AbstractSqlSupport {
    
    private static final String DISTINCT_STRING = "distinct"; //$NON-NLS-1$

    private String columnList;
    private Optional<String> whereClause;
    private Map<String, Object> parameters = new HashMap<>();
    private String distinct;
    private Optional<String> orderByClause;
    
    private SelectSupport(SqlTable table) {
        super(table);
    }
    
    public String getDistinct() {
        return distinct().orElse(EMPTY_STRING);
    }
    
    private Optional<String> distinct() {
        return Optional.ofNullable(distinct);
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
                + tableNameIncludingAlias()
                + whereClause().map(w -> ONE_SPACE + w).orElse(EMPTY_STRING)
                + orderByClause().map(o -> ONE_SPACE + o).orElse(EMPTY_STRING);
    }
    
    public static class Builder {
        private String distinct;
        private String orderByClause;
        private String whereClause;
        private Map<String, Object> parameters = new HashMap<>();
        private String columnList;
        private SqlTable table;
        
        public Builder isDistinct(boolean isDistinct) {
            if (isDistinct) {
                distinct = DISTINCT_STRING;
            }
            return this;
        }
        
        public Builder withOrderByClause(String orderByClause) {
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
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public SelectSupport build() {
            SelectSupport selectSupport = new SelectSupport(table);
            selectSupport.distinct = distinct;
            selectSupport.orderByClause = Optional.ofNullable(orderByClause);
            selectSupport.whereClause = Optional.ofNullable(whereClause);
            selectSupport.parameters = parameters;
            selectSupport.columnList = columnList;
            return selectSupport;
        }
    }
}
