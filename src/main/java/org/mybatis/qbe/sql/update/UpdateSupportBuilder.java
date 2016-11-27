package org.mybatis.qbe.sql.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.FieldAndValue;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.WhereSupportBuilder;

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
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new WhereBuilder(fieldsAndValues, field, condition, subCriteria);
        }
    }
    
    static class WhereBuilder extends WhereSupportBuilder.AbstractWhereBuilder<WhereBuilder> {
        private List<FieldAndValue<?>> setFieldsAndValues;
        
        public <T> WhereBuilder(List<FieldAndValue<?>> setFieldsAndValues, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(field, condition, subCriteria);
            this.setFieldsAndValues = setFieldsAndValues;
        }
        
        public UpdateSupport build() {
            return build(Function.identity(), Function.identity());
        }

        public UpdateSupport buildIgnoringAlias() {
            return build(SqlCriterion::ignoringAlias, FieldAndValue::ignoringAlias);
        }

        private UpdateSupport build(Function<SqlCriterion<?>, SqlCriterion<?>> criterionMapper,
                Function<FieldAndValue<?>, FieldAndValue<?>> fvMapper) {
            AtomicInteger sequence = new AtomicInteger(1);
            Map<String, Object> parameters = new HashMap<>();
            String setClause = renderSetValues(fvMapper, sequence, parameters);
            String whereClause = renderCriteria(criterionMapper, sequence, parameters);
            return UpdateSupport.of(setClause, whereClause, parameters);
        }
        
        private String renderSetValues(Function<FieldAndValue<?>, FieldAndValue<?>> mapper, AtomicInteger sequence, Map<String, Object> parameters) {
            List<String> phrases = new ArrayList<>();
            
            setFieldsAndValues.stream().map(mapper).forEach(fv -> {
                int number = sequence.getAndIncrement();
                SqlField<?> field = fv.getField();
                String phrase = String.format("%s = %s", field.render(), //$NON-NLS-1$
                        field.getParameterRenderer(number).render());
                phrases.add(phrase);
                parameters.put(String.format("p%s", number), fv.getValue()); //$NON-NLS-1$
            });
            
            return phrases.stream().collect(Collectors.joining(", ", "set ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        @Override
        public WhereBuilder getThis() {
            return this;
        }
    }
}
