package org.mybatis.qbe.mybatis3;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.WhereClause;
import org.mybatis.qbe.condition.Condition;
import org.mybatis.qbe.field.Field;
import org.mybatis.qbe.mybatis3.render.WhereClauseRenderer;

/**
 * This class combines the operations of building the where clause
 * and rendering it.  It is a shortcut to make the client code easier.
 * 
 * @author Jeff Butler
 *
 */
public class RenderingShortcut {

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
