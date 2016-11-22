package org.mybatis.qbe.sql.where.render;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.where.SqlCriterion;
import org.mybatis.qbe.sql.where.SqlField;
import org.mybatis.qbe.sql.where.WhereClause;

/**
 * This interface combines the operations of building the where clause
 * and rendering it.  It is a shortcut to make the client code easier.
 * 
 * @author Jeff Butler
 *
 */
public interface WhereClauseShortcut {

    static <T> Builder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
        return new Builder(field, condition, criteria);
    }
    
    static class Builder {
        
        private WhereClause.Builder whereClauseBuilder;

        public <T> Builder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            whereClauseBuilder = new WhereClause.Builder(field, condition, criteria);
        }
        
        public <T> Builder and(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            whereClauseBuilder.and(field, condition, criteria);
            return this;
        }
        
        public <T> Builder or(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            whereClauseBuilder.or(field, condition, criteria);
            return this;
        }
        
        public WhereSupport render() {
            return WhereClauseRenderer.of(whereClauseBuilder.build()).render();
        }

        public WhereSupport renderIgnoringAlias() {
            return WhereClauseRenderer.of(whereClauseBuilder.buildIgnoringAlias()).render();
        }
    }
}
