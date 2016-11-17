package org.mybatis.qbe.mybatis3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderedWhereClause {

    private String whereClause;
    private Map<String, Object> parameters = new HashMap<>();
    
    private RenderedWhereClause(String whereClause, Map<String, Object> parameters) {
        this.whereClause = whereClause;
        this.parameters.putAll(parameters);
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    
    public static RenderedWhereClause of(String whereClause, Map<String, Object> parameters) {
        return new RenderedWhereClause(whereClause, parameters);
    }
}
