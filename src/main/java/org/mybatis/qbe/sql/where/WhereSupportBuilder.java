package org.mybatis.qbe.sql.where;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.render.CriteriaRenderer;

public interface WhereSupportBuilder {

    static Builder whereSupport() {
        return new Builder();
    }
    
    static class Builder {
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new WhereBuilder(field, condition, subCriteria);
        }
    }
    
    static class WhereBuilder {
        
        private List<SqlCriterion<?>> criteria = new ArrayList<>();

        public <T> WhereBuilder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of(field, condition, subCriteria));
        }
        
        public <T> WhereBuilder and(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of("and", field, condition, subCriteria));
            return this;
        }
        
        public <T> WhereBuilder or(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            criteria.add(SqlCriterion.of("or", field, condition, subCriteria));
            return this;
        }
        
        public WhereSupport build() {
            return CriteriaRenderer.of(criteria.stream()).render();
        }

        public WhereSupport buildIgnoringAlias() {
            return CriteriaRenderer.of(criteria.stream().map(SqlCriterion::ignoringAlias)).render();
        }
    }
}
