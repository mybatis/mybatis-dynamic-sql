/**
 *    Copyright 2016-2019 the original author or authors.
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.InsertMapping;

public abstract class AbstractMultiRowInsertModel<T> {
    private SqlTable table;
    private List<T> records;
    private List<InsertMapping> columnMappings;
    
    protected AbstractMultiRowInsertModel(AbstractBuilder<T, ?> builder) {
        table = Objects.requireNonNull(builder.table);
        records = Collections.unmodifiableList(Objects.requireNonNull(builder.records));
        columnMappings = Objects.requireNonNull(builder.columnMappings);
    }

    public <R> Stream<R> mapColumnMappings(Function<InsertMapping, R> mapper) {
        return columnMappings.stream().map(mapper);
    }
    
    public List<T> records() {
        return records;
    }
    
    public SqlTable table() {
        return table;
    }
    
    public int recordCount() {
        return records.size();
    }
    
    public abstract static class AbstractBuilder<T, S extends AbstractBuilder<T, S>> {
        private SqlTable table;
        private List<T> records = new ArrayList<>();
        private List<InsertMapping> columnMappings = new ArrayList<>();
        
        public S withTable(SqlTable table) {
            this.table = table;
            return getThis();
        }
        
        public S withRecords(Collection<T> records) {
            this.records.addAll(records);
            return getThis();
        }
        
        public S withColumnMappings(List<InsertMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return getThis();
        }
        
        protected abstract S getThis();
    }
}
