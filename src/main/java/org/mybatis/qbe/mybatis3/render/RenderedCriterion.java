package org.mybatis.qbe.mybatis3.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderedCriterion {

    private String whereClauseFragment;
    private Map<String, Object> fragmentParameters = new HashMap<>();
    
    private RenderedCriterion(String whereClauseFragment, Map<String, Object> fragmentParameters) {
        this.whereClauseFragment = whereClauseFragment;
        this.fragmentParameters.putAll(fragmentParameters);
    }

    public String whereClauseFragment() {
        return whereClauseFragment;
    }

    public Map<String, Object> fragmentParameters() {
        return Collections.unmodifiableMap(fragmentParameters);
    }
    
    public static RenderedCriterion of(String whereClauseFragment, Map<String, Object> fragmentParameters) {
        return new RenderedCriterion(whereClauseFragment, fragmentParameters);
    }
}
