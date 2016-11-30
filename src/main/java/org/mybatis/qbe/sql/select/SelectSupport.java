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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SelectSupport {
    
    private static final String DISTINCT_STRING = "distinct"; //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private String whereClause;
    private Map<String, Object> parameters;
    private String distinct;
    private String orderByClause;
    
    private SelectSupport(boolean isDistinct, String whereClause, Map<String, Object> parameters, String orderByClause) {
        this.whereClause = whereClause;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
        initializeDistinct(isDistinct);
        initializeOrderByClause(orderByClause);
    }

    private void initializeDistinct(boolean isDistinct) {
        if (isDistinct) {
            distinct = DISTINCT_STRING;
        } else {
            distinct = EMPTY_STRING;
        }
    }

    private void initializeOrderByClause(String orderByClause) {
        if (orderByClause == null) {
            this.orderByClause = EMPTY_STRING;
        } else if (orderByClause.toLowerCase().startsWith("order by")){ //$NON-NLS-1$
            this.orderByClause = orderByClause;
        } else {
            this.orderByClause = String.format("order by %s", orderByClause); //$NON-NLS-1$
        }
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
    
    public static SelectSupport of(boolean isDistinct, String whereClause, Map<String, Object> parameters, String orderByClause) {
        return new SelectSupport(isDistinct, whereClause, parameters, orderByClause);
    }
}
