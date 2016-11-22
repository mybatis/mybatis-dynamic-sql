package org.mybatis.qbe.mybatis3;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.set.SetClause;
import org.mybatis.qbe.sql.set.render.RenderedSetClause;
import org.mybatis.qbe.sql.set.render.SetClauseRenderer;
import org.mybatis.qbe.sql.where.SqlCriterion;
import org.mybatis.qbe.sql.where.SqlField;
import org.mybatis.qbe.sql.where.WhereClause;
import org.mybatis.qbe.sql.where.render.RenderedWhereClause;
import org.mybatis.qbe.sql.where.render.WhereClauseRenderer;

public interface UpdateParameterShortcut {

    static <T> SetBuilder set(MyBatis3Field<T> field, T value) {
        return new SetBuilder(field, value);
    }
    
    static <T> SetBuilder setNull(MyBatis3Field<T> field) {
        return new SetBuilder(field);
    }
    
    static class SetBuilder extends SetClause.AbstractBuilder<SetBuilder> {
        public <T> SetBuilder(MyBatis3Field<T> field, T value) {
            super(field, value);
        }

        public <T> SetBuilder(MyBatis3Field<T> field) {
            super(field);
        }

        @Override
        public SetBuilder getThis() {
            return this;
        }
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            return new WhereBuilder(this, field, condition, criteria);
        }
    }
    
    static class WhereBuilder extends WhereClause.AbstractBuilder<WhereBuilder> {
        
        private SetBuilder setBuilder;
        
        public <T> WhereBuilder(SetBuilder setBuilder, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            super(field, condition, criteria);
            this.setBuilder = setBuilder;
        }
        
        public UpdateParameter render() {
            AtomicInteger sequence = new AtomicInteger(1);
            RenderedSetClause rsc = SetClauseRenderer.of(setBuilder.build()).render(sequence);
            RenderedWhereClause wwc = WhereClauseRenderer.of(build()).render(sequence);
            return UpdateParameter.of(rsc, wwc);
        }

        @Override
        public WhereBuilder getThis() {
            return this;
        }
    }
}
