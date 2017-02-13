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
package org.mybatis.dynamic.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class InsertSupportBuilder<T> {

    private T record;
    
    private InsertSupportBuilder(T record) {
        this.record = record;
    }
    
    public InsertSupportMappingBuilder<T> into(SqlTable table) {
        return new InsertSupportMappingBuilder<>(record, table);
    }
    
    public static <T> InsertSupportBuilder<T> insert(T record) {
        return new InsertSupportBuilder<>(record);
    }
    
    public static class InsertSupportMappingBuilder<T> {
        private T record;
        private SqlTable table;
        private List<InsertColumnMapping> columnMappings = new ArrayList<>();

        private InsertSupportMappingBuilder(T record, SqlTable table) {
            this.record = record;
            this.table = table;
        }
        
        public <F> InsertSupportMappingBuilderFinisher<F> map(SqlColumn<F> column) {
            return new InsertSupportMappingBuilderFinisher<>(column);
        }
        
        public InsertSupport<T> build() {
            return columnMappings.stream().collect(Collector.of(
                    InsertCollectorSupport::new,
                    InsertCollectorSupport::add,
                    InsertCollectorSupport::merge,
                    c -> c.toInsertSupport(record, table)));
        }
        
        public class InsertSupportMappingBuilderFinisher<F> {
            private SqlColumn<F> column;
            
            public InsertSupportMappingBuilderFinisher(SqlColumn<F> column) {
                this.column = column;
            }
            
            public InsertSupportMappingBuilder<T> toProperty(String property) {
                columnMappings.add(InsertColumnMapping.of(column, property));
                return InsertSupportMappingBuilder.this;
            }
            
            public InsertSupportMappingBuilder<T> toPropertyWhenPresent(String property, Supplier<F> valueSupplier) {
                if (valueSupplier.get() != null) {
                    columnMappings.add(InsertColumnMapping.of(column, property));
                }
                return InsertSupportMappingBuilder.this;
            }
            
            public InsertSupportMappingBuilder<T> toNull() {
                columnMappings.add(InsertColumnMapping.of(column));
                return InsertSupportMappingBuilder.this;
            }
        }
    }

    /**
     * A little pair to hold the column mapping.  Only intended for use in this builder. 
     *  
     * @param <F> the column type of this mapping
     */
    static class InsertColumnMapping {
        private SqlColumn<?> column;
        private String valuePhrase;
        
        static InsertColumnMapping of(SqlColumn<?> column) {
            InsertColumnMapping mapping = new InsertColumnMapping();
            mapping.column = column;
            mapping.valuePhrase = "null"; //$NON-NLS-1$
            return mapping;
        }
        
        static InsertColumnMapping of(SqlColumn<?> column, String property) {
            InsertColumnMapping mapping = new InsertColumnMapping();
            mapping.column = column;
            mapping.valuePhrase = column.getFormattedJdbcPlaceholder("record", property); //$NON-NLS-1$
            return mapping;
        }
    }
    
    static class InsertCollectorSupport {
        
        private List<String> columnPhrases = new ArrayList<>();
        private List<String> valuePhrases = new ArrayList<>();
        
        void add(InsertColumnMapping mapping) {
            columnPhrases.add(mapping.column.name());
            valuePhrases.add(mapping.valuePhrase);
        }
        
        InsertCollectorSupport merge(InsertCollectorSupport other) {
            columnPhrases.addAll(other.columnPhrases);
            valuePhrases.addAll(other.valuePhrases);
            return this;
        }
        
        String columnsPhrase() {
            return columnPhrases.stream()
                    .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
        }

        String valuesPhrase() {
            return valuePhrases.stream()
                    .collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        
        <T> InsertSupport<T> toInsertSupport(T record, SqlTable table) {
            return InsertSupport.of(columnsPhrase(), valuesPhrase(), record, table);
        }
    }
}
