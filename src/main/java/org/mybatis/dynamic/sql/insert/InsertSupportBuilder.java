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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.BeanPropertyGetter;

public class InsertSupportBuilder<T> {

    private T record;
    
    private InsertSupportBuilder(T record) {
        this.record = record;
    }
    
    public InsertSupportMappingBuilder into(SqlTable table) {
        return new InsertSupportMappingBuilder(table);
    }
    
    public static <T> InsertSupportBuilder<T> of(T record) {
        return new InsertSupportBuilder<>(record);
    }
    
    public class InsertSupportMappingBuilder {
        private List<InsertColumnMapping> columnMappings = new ArrayList<>();
        private SqlTable table;

        private InsertSupportMappingBuilder(SqlTable table) {
            this.table = table;
        }
        
        public <F> InsertSupportMappingBuilderFinisher<F> map(SqlColumn<F> column) {
            return new InsertSupportMappingBuilderFinisher<>(column);
        }
        
        public InsertSupport<T> build() {
            return columnMappings.stream()
                    .collect(InsertColumnMappingCollector.toInsertSupport(record, table));
        }
        
        public class InsertSupportMappingBuilderFinisher<F> {
            private SqlColumn<F> column;
            
            public InsertSupportMappingBuilderFinisher(SqlColumn<F> column) {
                this.column = column;
            }
            
            public InsertSupportMappingBuilder toProperty(String property) {
                columnMappings.add(InsertColumnMapping.of(column, property));
                return InsertSupportMappingBuilder.this;
            }
            
            public InsertSupportMappingBuilder toPropertyWhenPresent(String property) {
                if (BeanPropertyGetter.instance().getPropertyValue(record, property) != null) {
                    columnMappings.add(InsertColumnMapping.of(column, property));
                }
                return InsertSupportMappingBuilder.this;
            }
            
            public InsertSupportMappingBuilder toNull() {
                columnMappings.add(InsertColumnMapping.of(column));
                return InsertSupportMappingBuilder.this;
            }
        }
    }
}
