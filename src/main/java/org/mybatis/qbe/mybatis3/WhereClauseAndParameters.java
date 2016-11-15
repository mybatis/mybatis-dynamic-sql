package org.mybatis.qbe.mybatis3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.CriterionContainer;
import org.mybatis.qbe.condition.Condition;
import org.mybatis.qbe.condition.Conditions;
import org.mybatis.qbe.field.Field;
import org.mybatis.qbe.mybatis3.render.MyBatis3Renderer;

public class WhereClauseAndParameters {

    private String whereClause;
    private Map<String, Object> parameters = new HashMap<>();
    
    private WhereClauseAndParameters(String whereClause, Map<String, Object> parameters) {
        this.whereClause = whereClause;
        this.parameters.putAll(parameters);
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    
    public static WhereClauseAndParameters of(String whereClause, Map<String, Object> parameters) {
        return new WhereClauseAndParameters(whereClause, parameters);
    }

    public static <T> Builder where(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
        return new Builder(field, condition, criteria);
    }
    
    public static class Builder {

        private List<Criterion<?>> criteria = new ArrayList<>();
        
        public <T> Builder(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
            this.criteria.add(Criterion.of(field, condition, criteria));
        }
        
        public <T> Builder and(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
            this.criteria.add(Conditions.and(field, condition, criteria));
            return this;
        }
        
        public <T> Builder or(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
            this.criteria.add(Conditions.or(field, condition, criteria));
            return this;
        }
        
        public WhereClauseAndParameters build() {
            return buildRenderer().render();
        }

        public WhereClauseAndParameters buildWithoutTableAlias() {
            return buildRenderer().renderWithoutTableAlias();
        }
        
        private MyBatis3Renderer buildRenderer() {
            return MyBatis3Renderer.of(CriterionContainer.of(criteria.stream()));
        }
    }
}
