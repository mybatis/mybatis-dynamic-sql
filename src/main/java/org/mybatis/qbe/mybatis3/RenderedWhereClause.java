package org.mybatis.qbe.mybatis3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderedWhereClause {

    private String whereClause;
    private Map<String, Object> parameters;
    
    private RenderedWhereClause(String whereClause, Map<String, Object> parameters) {
        this.whereClause = whereClause;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public static RenderedWhereClause of(String whereClause, Map<String, Object> parameters) {
        return new RenderedWhereClause(whereClause, parameters);
    }
}
