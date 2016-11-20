package org.mybatis.qbe.sql.render;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.WhereClause;

/**
 * This interface combines the operations of building the where clause
 * and rendering it.  It is a shortcut to make the client code easier.
 * 
 * @author Jeff Butler
 *
 */
public interface RenderingShortcut {

    static <T> Builder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
        return new Builder(field, condition, criteria);
    }
    
    static <T> AliasIgnoringBuilder whereIgnoringAlias(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
        return new AliasIgnoringBuilder(field, condition, criteria);
    }
    
    static class Builder extends WhereClause.AbstractBuilder<Builder> {

        public <T> Builder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            super(field, condition, criteria);
        }
        
        public RenderedWhereClause render() {
            return WhereClauseRenderer.of(super.build()).render();
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }

    static class AliasIgnoringBuilder extends WhereClause.AbstractAliasIgnoringBuilder<AliasIgnoringBuilder> {

        public <T> AliasIgnoringBuilder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            super(field, condition, criteria);
        }
        
        public RenderedWhereClause render() {
            return WhereClauseRenderer.of(super.build()).render();
        }

        @Override
        public AliasIgnoringBuilder getThis() {
            return this;
        }
    }
}
