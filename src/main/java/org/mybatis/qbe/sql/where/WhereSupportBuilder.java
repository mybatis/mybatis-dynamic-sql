package org.mybatis.qbe.sql.where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;
import org.mybatis.qbe.sql.where.render.RenderedCriterion;

public interface WhereSupportBuilder {

    static Builder whereSupport() {
        return new Builder();
    }
    
    static class Builder {
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new WhereBuilder(field, condition, subCriteria);
        }
    }
    
    abstract static class AbstractWhereBuilder<T extends AbstractWhereBuilder<T>> {
        private List<SqlCriterion<?>> criteria = new ArrayList<>();
        
        protected <S> AbstractWhereBuilder(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of(field, condition, subCriteria));
        }
        
        public <S> T and(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of("and", field, condition, subCriteria));
            return getThis();
        }
        
        public <S> T or(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of("or", field, condition, subCriteria));
            return getThis();
        }
        
        protected String renderCriteria(Function<SqlCriterion<?>, SqlCriterion<?>> mapper, AtomicInteger sequence, Map<String, Object> parameters) {
            StringBuilder buffer = new StringBuilder("where");
            
            criteria.stream().map(mapper).forEach(c -> {
                RenderedCriterion rc = CriterionRenderer.of(c, sequence).render();
                buffer.append(rc.whereClauseFragment());
                parameters.putAll(rc.fragmentParameters());
            });
            return buffer.toString();
        }
        
        public abstract T getThis();
    }
    
    static class WhereBuilder extends AbstractWhereBuilder<WhereBuilder> {
        public <T> WhereBuilder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(field, condition, subCriteria);
        }
        
        public WhereSupport build() {
            return build(Function.identity());
        }

        public WhereSupport buildIgnoringAlias() {
            return build(SqlCriterion::ignoringAlias);
        }
        
        private WhereSupport build(Function<SqlCriterion<?>, SqlCriterion<?>> mapper) {
            AtomicInteger sequence = new AtomicInteger(1);
            Map<String, Object> parameters = new HashMap<>();
            String whereClause = renderCriteria(mapper, sequence, parameters);
            return WhereSupport.of(whereClause, parameters);
        }
        
        @Override
        public WhereBuilder getThis() {
            return this;
        }
    }
}
