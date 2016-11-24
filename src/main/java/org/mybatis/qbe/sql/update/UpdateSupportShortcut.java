package org.mybatis.qbe.sql.update;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.FieldValuePair;
import org.mybatis.qbe.sql.FieldValuePairList;
import org.mybatis.qbe.sql.set.render.SetSupport;
import org.mybatis.qbe.sql.set.render.SetValuesRenderer;
import org.mybatis.qbe.sql.where.SqlCriterion;
import org.mybatis.qbe.sql.where.SqlField;
import org.mybatis.qbe.sql.where.WhereClause;
import org.mybatis.qbe.sql.where.render.WhereClauseRenderer;
import org.mybatis.qbe.sql.where.render.WhereSupport;

public interface UpdateSupportShortcut {

    static SetBuilder update() {
        return new SetBuilder();
    }
    
    static class SetBuilder {
        private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();
        
        public SetBuilder() {
            super();
        }
        
        public <T> SetBuilder setIfPresent(SqlField<T> field, T value) {
            if (value != null) {
                set(field, value);
            }
            return this;
        }
        
        public <T> SetBuilder set(SqlField<T> field, T value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
            return this;
        }
        
        public <T> SetBuilder setNull(SqlField<T> field) {
            set(field, null);
            return this;
        }
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            FieldValuePairList.Builder setValuesBuilder = new FieldValuePairList.Builder()
                    .withFieldValuePairs(fieldValuePairs.stream());
            
            return new WhereBuilder(setValuesBuilder, field, condition, criteria);
        }
    }
    
    static class WhereBuilder {
        private FieldValuePairList.Builder setValuesBuilder;
        private WhereClause.Builder whereClauseBuilder;
        
        public <T> WhereBuilder(FieldValuePairList.Builder setValuesBuilder, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
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
