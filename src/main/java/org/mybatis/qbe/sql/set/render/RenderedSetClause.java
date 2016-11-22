package org.mybatis.qbe.sql.set.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderedSetClause {

    private String setClause;
    private Map<String, Object> parameters;
    
    private RenderedSetClause(String setClause, Map<String, Object> parameters) {
        this.setClause = setClause;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    public String getSetClause() {
        return setClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public static RenderedSetClause of(String setClause, Map<String, Object> parameters) {
        return new RenderedSetClause(setClause, parameters);
    }
}
