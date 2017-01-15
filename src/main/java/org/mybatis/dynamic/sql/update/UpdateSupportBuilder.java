/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.where.AbstractWhereBuilder;
import org.mybatis.dynamic.sql.where.WhereSupport;

public interface UpdateSupportBuilder {

    static UpdateSupportBuildStep1 update(SqlTable table) {
        return new UpdateSupportBuildStep1(table);
    }
    
    static class UpdateSupportBuildStep1 {
        private int id = 1;
        private List<ColumnAndValue<?>> columnsAndValues = new ArrayList<>();
        private SqlTable table;
        
        public UpdateSupportBuildStep1(SqlTable table) {
            this.table = table;
        }
        
        public <T> UpdateSupportBuildStep1 set(SqlColumn<T> column, Optional<T> value) {
            value.ifPresent(v -> columnsAndValues.add(ColumnAndValue.of(column, v, id++)));
            return this;
        }
        
        public <T> UpdateSupportBuildStep1 set(SqlColumn<T> column, T value) {
            columnsAndValues.add(ColumnAndValue.of(column, value, id++));
            return this;
        }
        
        public <T> UpdateSupportBuildStep1 setNull(SqlColumn<T> column) {
            columnsAndValues.add(ColumnAndValue.of(column, id++));
            return this;
        }
        
        public <T> UpdateSupportBuildStep2 where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new UpdateSupportBuildStep2(table, columnsAndValues, column, condition, subCriteria);
        }
    }
    
    static class UpdateSupportBuildStep2 extends AbstractWhereBuilder<UpdateSupportBuildStep2> {
        private List<ColumnAndValue<?>> setColumnsAndValues;
        private SqlTable table;
        
        public <T> UpdateSupportBuildStep2(SqlTable table, List<ColumnAndValue<?>> setColumnsAndValues, SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
            this.table = table;
            this.setColumnsAndValues = setColumnsAndValues;
        }
        
        public UpdateSupport build() {
            Map<String, Object> parameters = new HashMap<>();
            SetValuesCollector setValuesCollector = renderSetValues();
            WhereSupport whereSupport = renderCriteria(SqlColumn::name);
            parameters.putAll(setValuesCollector.parameters);
            parameters.putAll(whereSupport.getParameters());
            return UpdateSupport.of(setValuesCollector.getSetClause(), whereSupport.getWhereClause(), parameters, table);
        }
        
        private SetValuesCollector renderSetValues() {
            return setColumnsAndValues.stream().collect(Collector.of(
                    SetValuesCollector::new,
                    SetValuesCollector::add,
                    SetValuesCollector::merge));
        }

        @Override
        public UpdateSupportBuildStep2 getThis() {
            return this;
        }
        
        static class SetValuesCollector {
            List<String> phrases = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();
            
            void add(ColumnAndValue<?> columnAndValue) {
                phrases.add(columnAndValue.setPhrase);
                parameters.put(columnAndValue.mapKey, columnAndValue.value);
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
    
    static class ColumnAndValue<T> {
        private T value;
        private String setPhrase;
        private String mapKey;
        
        private ColumnAndValue(SqlColumn<T> column, T value, int uniqueId) {
            this.value = value;
            String jdbcPlaceholder = column.getFormattedJdbcPlaceholder(String.format("parameters.up%s", uniqueId)); //$NON-NLS-1$
            setPhrase = String.format("%s = %s", column.name(), //$NON-NLS-1$
                    jdbcPlaceholder);
            mapKey = String.format("up%s", uniqueId); //$NON-NLS-1$
        }
        
        static <T> ColumnAndValue<T> of(SqlColumn<T> column, T value, int uniqueId) {
            return new ColumnAndValue<>(column, value, uniqueId);
        }

        static <T> ColumnAndValue<T> of(SqlColumn<T> column, int uniqueId) {
            return new ColumnAndValue<>(column, null, uniqueId);
        }
    }
}
