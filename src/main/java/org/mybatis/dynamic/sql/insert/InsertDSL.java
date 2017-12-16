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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.InsertMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;

public class InsertDSL<T> {

    private T record;
    private SqlTable table;
    private List<InsertMapping> columnMappings = new ArrayList<>();
    
    private InsertDSL(T record, SqlTable table) {
        this.record = record;
        this.table = table;
    }
    
    public <F> ColumnMappingFinisher<F> map(SqlColumn<F> column) {
        return new ColumnMappingFinisher<>(column);
    }
    
    public InsertModel<T> build() {
        return InsertModel.withRecord(record)
                .withTable(table)
                .withColumnMappings(columnMappings)
                .build();
    }
    
    public static <T> IntoGatherer<T> insert(T record) {
        return new IntoGatherer<>(record);
    }

    public static class IntoGatherer<T> {
        private T record;
        
        private IntoGatherer(T record) {
            this.record = record;
        }

        public InsertDSL<T> into(SqlTable table) {
            return new InsertDSL<>(record, table);
        }
    }
    
    public class ColumnMappingFinisher<F> {
        private SqlColumn<F> column;
            
        public ColumnMappingFinisher(SqlColumn<F> column) {
            this.column = column;
        }
            
        public InsertDSL<T> toProperty(String property) {
            columnMappings.add(PropertyMapping.of(column, property));
            return InsertDSL.this;
        }
            
        public InsertDSL<T> toPropertyWhenPresent(String property, Supplier<?> valueSupplier) {
            if (valueSupplier.get() != null) {
                toProperty(property);
            }
            return InsertDSL.this;
        }
            
        public InsertDSL<T> toNull() {
            columnMappings.add(NullMapping.of(column));
            return InsertDSL.this;
        }
            
        public InsertDSL<T> toConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return InsertDSL.this;
        }
            
        public InsertDSL<T> toStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return InsertDSL.this;
        }
    }
}
