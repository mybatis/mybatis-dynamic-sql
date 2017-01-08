/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.select;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.AbstractSqlSupport;
import org.mybatis.qbe.sql.SqlTable;

public class SelectSupport extends AbstractSqlSupport {
    
    private static final String DISTINCT_STRING = "distinct"; //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private String columnList;
    private String whereClause;
    private Map<String, Object> parameters = new HashMap<>();
    private String distinct;
    private String orderByClause;
    
    private SelectSupport(SqlTable table) {
        super(table);
    }
    
    public String getDistinct() {
        return distinct;
    }
    
    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getOrderByClause() {
        return orderByClause;
    }
    
    public String getColumnList() {
        return columnList;
    }
    
    public String getFullSelectStatement() {
        return Stream.of("select", //$NON-NLS-1$
                distinct,
                getColumnList(),
                "from", //$NON-NLS-1$
                table().orElse(UNKNOWN_TABLE).nameIncludingAlias(),
                getWhereClause(),
                getOrderByClause()).collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    public static class Builder {
        private String distinctString = EMPTY_STRING;
        private String orderByClause = EMPTY_STRING;
        private String whereClause = EMPTY_STRING;
        private Map<String, Object> parameters;
        private String columnList;
        private SqlTable table;
        
        public Builder isDistinct() {
            distinctString = DISTINCT_STRING;
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
            this.parameters = parameters;
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
            selectSupport.distinct = distinctString;
            selectSupport.orderByClause = orderByClause;
            selectSupport.whereClause = whereClause;
            selectSupport.parameters = parameters;
            selectSupport.columnList = columnList;
            return selectSupport;
        }
    }
}
