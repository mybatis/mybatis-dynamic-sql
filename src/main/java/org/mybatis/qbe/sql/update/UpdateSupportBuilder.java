package org.mybatis.qbe.sql.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.FieldAndValue;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.WhereClause;
import org.mybatis.qbe.sql.where.WhereSupport;
import org.mybatis.qbe.sql.where.render.WhereClauseRenderer;

public interface UpdateSupportBuilder {

    static SetBuilder updateSupport() {
        return new SetBuilder();
    }
    
    static class SetBuilder {
        private List<FieldAndValue<?>> fieldsAndValues = new ArrayList<>();
        
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
            fieldsAndValues.add(FieldAndValue.of(field, value));
            return this;
        }
        
        public <T> SetBuilder setNull(SqlField<T> field) {
            set(field, null);
            return this;
        }
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            return new WhereBuilder(fieldsAndValues, field, condition, criteria);
        }
    }
    
    static class WhereBuilder {
        private List<FieldAndValue<?>> setFieldsAndValues;
        private WhereClause.Builder whereClauseBuilder;
        
        public <T> WhereBuilder(List<FieldAndValue<?>> setFieldsAndValues, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            whereClauseBuilder = new WhereClause.Builder(field, condition, criteria);
            this.setFieldsAndValues = setFieldsAndValues;
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
            Map<String, Object> parameters = new HashMap<>();
            String setClause = renderSetValues(setFieldsAndValues.stream(), sequence, parameters);
            WhereSupport wwc = WhereClauseRenderer.of(whereClauseBuilder.build()).render(sequence);
            parameters.putAll(wwc.getParameters());
            return UpdateSupport.of(setClause, wwc.getWhereClause(), parameters);
        }

        public UpdateSupport buildIgnoringAlias() {
            AtomicInteger sequence = new AtomicInteger(1);
            Map<String, Object> parameters = new HashMap<>();
            String setClause = renderSetValues(setFieldsAndValues.stream().map(FieldAndValue::ignoringAlias), sequence, parameters);
            WhereSupport wwc = WhereClauseRenderer.of(whereClauseBuilder.buildIgnoringAlias()).render(sequence);
            parameters.putAll(wwc.getParameters());
            return UpdateSupport.of(setClause, wwc.getWhereClause(), parameters);
        }

        private String renderSetValues(Stream<FieldAndValue<?>> setFieldsAndValues, AtomicInteger sequence, Map<String, Object> parameters) {
            List<String> phrases = new ArrayList<>();
            
            setFieldsAndValues.forEach(fv -> {
                int number = sequence.getAndIncrement();
                SqlField<?> field = fv.getField();
                String phrase = String.format("%s = %s", field.render(),
                        field.getParameterRenderer(number).render());
                phrases.add(phrase);
                parameters.put(String.format("p%s", number), fv.getValue());
            });
            
            return phrases.stream().collect(Collectors.joining(", ", "set ", ""));
        }
    }
}
