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

import java.util.List;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.InsertMapping;

public class InsertModel<T> {
    private SqlTable table;
    private T record;
    private List<InsertMapping> columnMappings;
    
    private InsertModel() {
        super();
    }

    public Stream<InsertMapping> columnMappings() {
        return columnMappings.stream();
    }
    
    public T record() {
        return record;
    }
    
    public SqlTable table() {
        return table;
    }
    
    public static class Builder<T> {
        private SqlTable table;
        private T record;
        private List<InsertMapping> columnMappings;
        
        public Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder<T> withRecord(T record) {
            this.record = record;
            return this;
        }
        
        public Builder<T> withColumnMappings(List<InsertMapping> columnMappings) {
            this.columnMappings = columnMappings;
            return this;
        }
        
        public InsertModel<T> build() {
            InsertModel<T> insertModel = new InsertModel<>();
            insertModel.table = table;
            insertModel.record = record;
            insertModel.columnMappings = columnMappings;
            return insertModel;
        }
    }
}
