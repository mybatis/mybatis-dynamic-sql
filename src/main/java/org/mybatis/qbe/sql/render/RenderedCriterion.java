package org.mybatis.qbe.sql.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderedCriterion {

    private String whereClauseFragment;
    private Map<String, Object> fragmentParameters;
    
    private RenderedCriterion(String whereClauseFragment, Map<String, Object> fragmentParameters) {
        this.whereClauseFragment = whereClauseFragment;
        this.fragmentParameters = Collections.unmodifiableMap(new HashMap<>(fragmentParameters));
    }

    public String whereClauseFragment() {
        return whereClauseFragment;
    }

    public Map<String, Object> fragmentParameters() {
        return fragmentParameters;
    }
    
    public static RenderedCriterion of(String whereClauseFragment, Map<String, Object> fragmentParameters) {
        return new RenderedCriterion(whereClauseFragment, fragmentParameters);
    }
}
