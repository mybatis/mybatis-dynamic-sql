/**
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.qbe.sql.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.AbstractWhereBuilder;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface UpdateSupportBuilder {

    static SetBuilder updateSupport() {
        return new SetBuilder();
    }
    
    static class SetBuilder {
        private AtomicInteger sequence = new AtomicInteger(1);
        private List<FieldAndValue<?>> fieldsAndValues = new ArrayList<>();
        
        public SetBuilder() {
            super();
        }
        
        public <T> SetBuilder set(SqlField<T> field, Optional<T> value) {
            value.ifPresent(v -> fieldsAndValues.add(FieldAndValue.of(field, v, sequence.getAndIncrement())));
            return this;
        }
        
        public <T> SetBuilder set(SqlField<T> field, T value) {
            fieldsAndValues.add(FieldAndValue.of(field, value, sequence.getAndIncrement()));
            return this;
        }
        
        public <T> SetBuilder setNull(SqlField<T> field) {
            fieldsAndValues.add(FieldAndValue.of(field, sequence.getAndIncrement()));
            return this;
        }
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new WhereBuilder(fieldsAndValues, field, condition, subCriteria);
        }
    }
    
    static class WhereBuilder extends AbstractWhereBuilder<WhereBuilder> {
        private List<FieldAndValue<?>> setFieldsAndValues;
        
        public <T> WhereBuilder(List<FieldAndValue<?>> setFieldsAndValues, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(field, condition, subCriteria);
            this.setFieldsAndValues = setFieldsAndValues;
        }
        
        public UpdateSupport build() {
            Map<String, Object> parameters = new HashMap<>();
            SetValuesCollector setValuesCollector = renderSetValues();
            WhereSupport whereSupport = renderCriteria(SqlField::nameIgnoringTableAlias);
            parameters.putAll(setValuesCollector.parameters);
            parameters.putAll(whereSupport.getParameters());
            return UpdateSupport.of(setValuesCollector.getSetClause(), whereSupport.getWhereClause(), parameters);
        }
        
        private SetValuesCollector renderSetValues() {
            return setFieldsAndValues.stream().collect(Collector.of(
                    SetValuesCollector::new,
                    SetValuesCollector::add,
                    SetValuesCollector::merge));
        }

        @Override
        public WhereBuilder getThis() {
            return this;
        }
        
        static class SetValuesCollector {
            List<String> phrases = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();
            
            void add(FieldAndValue<?> fieldAndValue) {
                phrases.add(fieldAndValue.setPhrase);
                parameters.put(fieldAndValue.mapKey, fieldAndValue.value);
            }
            
            SetValuesCollector merge(SetValuesCollector other) {
                phrases.addAll(other.phrases);
                parameters.putAll(other.parameters);
                return this;
            }

            String getSetClause() {
                return phrases.stream().collect(Collectors.joining(", ", "set ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }
    
    static class FieldAndValue<T> {
        private T value;
        private String setPhrase;
        private String mapKey;
        
        private FieldAndValue(SqlField<T> field, T value, int uniqueId) {
            this.value = value;
            String jdbcPlaceholder = field.getFormattedJdbcPlaceholder(String.format("parameters.up%s", uniqueId)); //$NON-NLS-1$
            setPhrase = String.format("%s = %s", field.nameIgnoringTableAlias(), //$NON-NLS-1$
                    jdbcPlaceholder);
            mapKey = String.format("up%s", uniqueId); //$NON-NLS-1$
        }
        
        static <T> FieldAndValue<T> of(SqlField<T> field, T value, int uniqueId) {
            return new FieldAndValue<>(field, value, uniqueId);
        }

        static <T> FieldAndValue<T> of(SqlField<T> field, int uniqueId) {
            return new FieldAndValue<>(field, null, uniqueId);
        }
    }
}
