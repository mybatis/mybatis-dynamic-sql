package org.mybatis.qbe.mybatis3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.WhereClause;
import org.mybatis.qbe.condition.Condition;
import org.mybatis.qbe.field.Field;
import org.mybatis.qbe.mybatis3.render.WhereClauseRenderer;

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

    public static <T> Builder where(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
        return new Builder(field, condition, criteria);
    }
    
    public static class Builder extends WhereClause.AbstractBuilder<Builder> {

        public <T> Builder(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
            super(field, condition, criteria);
        }
        
        public RenderedWhereClause render() {
            return buildRenderer().render();
        }

        public RenderedWhereClause renderWithoutTableAlias() {
            return buildRenderer().renderWithoutTableAlias();
        }
        
        private WhereClauseRenderer buildRenderer() {
            return WhereClauseRenderer.of(super.build());
        }
        
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
