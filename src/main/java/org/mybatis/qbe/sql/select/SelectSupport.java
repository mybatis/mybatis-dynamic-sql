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

public class SelectSupport {
    
    private static final String DISTINCT_STRING = "distinct"; //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private String whereClause = EMPTY_STRING;
    private Map<String, Object> parameters = new HashMap<>();
    private String distinct = EMPTY_STRING;
    private String orderByClause = EMPTY_STRING;
    
    private SelectSupport() {
        super();
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
    
    public static class Builder {
        private SelectSupport selectSupport = new SelectSupport();
        
        public Builder isDistinct() {
            selectSupport.distinct = DISTINCT_STRING;
            return this;
        }
        
        public Builder withOrderByClause(String orderByClause) {
            if (orderByClause.toLowerCase().startsWith("order by")){ //$NON-NLS-1$
                selectSupport.orderByClause = orderByClause;
            } else {
                selectSupport.orderByClause = String.format("order by %s", orderByClause); //$NON-NLS-1$
            }
            return this;
        }
        
        public Builder withWhereClause(String whereClause) {
            selectSupport.whereClause = whereClause;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            selectSupport.parameters = parameters;
            return this;
        }
        
        public SelectSupport build() {
            return selectSupport;
        }
    }
}
