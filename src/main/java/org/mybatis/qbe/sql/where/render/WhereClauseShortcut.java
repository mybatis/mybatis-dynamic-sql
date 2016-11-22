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
    
    static class Builder extends WhereClause.AbstractBuilder<Builder> {

        public <T> Builder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            super(field, condition, criteria);
        }
        
        public WhereSupport render() {
            return WhereClauseRenderer.of(build()).render();
        }

        public WhereSupport renderIgnoringAlias() {
            return WhereClauseRenderer.of(buildIgnoringAlias()).render();
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }
}
