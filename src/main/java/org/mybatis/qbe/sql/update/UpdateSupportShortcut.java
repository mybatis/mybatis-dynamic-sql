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
import org.mybatis.qbe.sql.where.render.WhereClauseRenderer;
import org.mybatis.qbe.sql.where.render.WhereSupport;

public interface UpdateSupportShortcut {

    static <T> SetBuilder set(MyBatis3Field<T> field, T value) {
        return new SetBuilder(field, value);
    }
    
    static <T> SetBuilder setNull(MyBatis3Field<T> field) {
        return new SetBuilder(field);
    }
    
    static class SetBuilder {
        private SetValues.Builder setValuesBuilder;
        
        public <T> SetBuilder(MyBatis3Field<T> field) {
            setValuesBuilder = new SetValues.Builder(field);
        }

        public <T> SetBuilder(MyBatis3Field<T> field, T value) {
            setValuesBuilder = new SetValues.Builder(field, value);
        }

        public <T> SetBuilder andSet(SqlField<T> field, T value) {
            setValuesBuilder.andSet(field, value);
            return this;
        }
        
        public <T> SetBuilder andSetNull(SqlField<T> field) {
            setValuesBuilder.andSetNull(field);
            return this;
        }
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            return new WhereBuilder(setValuesBuilder, field, condition, criteria);
        }
    }
    
    static class WhereBuilder {
        private SetValues.Builder setValuesBuilder;
        private WhereClause.Builder whereClauseBuilder;
        
        public <T> WhereBuilder(SetValues.Builder setValuesBuilder, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            whereClauseBuilder = new WhereClause.Builder(field, condition, criteria);
            this.setValuesBuilder = setValuesBuilder;
        }
        
        public <T> WhereBuilder and(SqlField<T> field, Condition<T> condition, SqlCriterion<?>... criteria) {
            whereClauseBuilder.and(field, condition, criteria);
            return this;
        }

        public <T> WhereBuilder or(SqlField<T> field, Condition<T> condition, SqlCriterion<?>... criteria) {
            whereClauseBuilder.or(field, condition, criteria);
            return this;
        }
        
        public UpdateSupport build() {
            AtomicInteger sequence = new AtomicInteger(1);
            SetSupport setSupport = SetValuesRenderer.of(setValuesBuilder.build()).render(sequence);
            WhereSupport wwc = WhereClauseRenderer.of(whereClauseBuilder.build()).render(sequence);
            return UpdateSupport.of(setSupport, wwc);
        }

        public UpdateSupport buildIgnoringAlias() {
            AtomicInteger sequence = new AtomicInteger(1);
            SetSupport setSupport = SetValuesRenderer.of(setValuesBuilder.buildIgnoringAlias()).render(sequence);
            WhereSupport wwc = WhereClauseRenderer.of(whereClauseBuilder.buildIgnoringAlias()).render(sequence);
            return UpdateSupport.of(setSupport, wwc);
        }
    }
}
