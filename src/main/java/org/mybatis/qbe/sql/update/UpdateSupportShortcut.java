package org.mybatis.qbe.sql.update;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.set.SetValues;
import org.mybatis.qbe.sql.set.render.SetSupport;
import org.mybatis.qbe.sql.set.render.SetValuesRenderer;
import org.mybatis.qbe.sql.where.SqlCriterion;
import org.mybatis.qbe.sql.where.SqlField;
import org.mybatis.qbe.sql.where.WhereClause;
import org.mybatis.qbe.sql.where.render.WhereSupport;
import org.mybatis.qbe.sql.where.render.WhereClauseRenderer;

public interface UpdateSupportShortcut {

    static <T> SetBuilder set(MyBatis3Field<T> field, T value) {
        return new SetBuilder(field, value);
    }
    
    static <T> SetBuilder setNull(MyBatis3Field<T> field) {
        return new SetBuilder(field);
    }
    
    static class SetBuilder extends SetValues.AbstractBuilder<SetBuilder> {
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
        
        public UpdateSupport render() {
            AtomicInteger sequence = new AtomicInteger(1);
            SetSupport setSupport = SetValuesRenderer.of(setBuilder.build()).render(sequence);
            WhereSupport wwc = WhereClauseRenderer.of(build()).render(sequence);
            return UpdateSupport.of(setSupport, wwc);
        }

        public UpdateSupport renderIgnoringAlias() {
            AtomicInteger sequence = new AtomicInteger(1);
            SetSupport setSupport = SetValuesRenderer.of(setBuilder.buildIgnoringAlias()).render(sequence);
            WhereSupport wwc = WhereClauseRenderer.of(buildIgnoringAlias()).render(sequence);
            return UpdateSupport.of(setSupport, wwc);
        }
        
        @Override
        public WhereBuilder getThis() {
            return this;
        }
    }
}
