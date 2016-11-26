package org.mybatis.qbe.sql.where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
        
        public <S> T and(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of("and", field, condition, subCriteria));
            return getThis();
        }
        
        public <S> T or(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of("or", field, condition, subCriteria));
            return getThis();
        }
        
        protected void addCriterion(SqlCriterion<?> criterion) {
            criteria.add(criterion);
        }
        
        protected String renderCriteria(AtomicInteger sequence, Map<String, Object> parameters) {
            return renderCriteria(criteria.stream(), sequence, parameters);
        }
        
        protected String renderCriteriaIgnoringAlias(AtomicInteger sequence, Map<String, Object> parameters) {
            return renderCriteria(criteria.stream().map(SqlCriterion::ignoringAlias), sequence, parameters);
        }
        
        private static String renderCriteria(Stream<SqlCriterion<?>> criteria, AtomicInteger sequence, Map<String, Object> parameters) {
            StringBuilder buffer = new StringBuilder("where");
            
            criteria.forEach(c -> {
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
            addCriterion(SqlCriterion.of(field, condition, subCriteria));
        }
        
        public WhereSupport build() {
            AtomicInteger sequence = new AtomicInteger(1);
            Map<String, Object> parameters = new HashMap<>();
            String whereClause = renderCriteria(sequence, parameters);
            return WhereSupport.of(whereClause, parameters);
        }

        public WhereSupport buildIgnoringAlias() {
            AtomicInteger sequence = new AtomicInteger(1);
            Map<String, Object> parameters = new HashMap<>();
            String whereClause = renderCriteriaIgnoringAlias(sequence, parameters);
            return WhereSupport.of(whereClause, parameters);
        }
        
        @Override
        public WhereBuilder getThis() {
            return this;
        }
    }
}
