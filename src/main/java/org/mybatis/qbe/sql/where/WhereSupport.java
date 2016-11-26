package org.mybatis.qbe.sql.where;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WhereSupport {

    private String whereClause;
    private Map<String, Object> parameters;
    
    private WhereSupport(String whereClause, Map<String, Object> parameters) {
        this.whereClause = whereClause;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public static WhereSupport of(String whereClause, Map<String, Object> parameters) {
        return new WhereSupport(whereClause, parameters);
    }
}
