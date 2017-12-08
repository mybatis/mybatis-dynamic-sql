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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.BatchInsertRenderer;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.InsertMapping;

public class BatchInsertModel<T> {
    private SqlTable table;
    private List<T> records;
    private List<InsertMapping> columnMappings;
    
    private BatchInsertModel(Builder<T> builder) {
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
    
    public BatchInsert<T> render(RenderingStrategy renderingStrategy) {
        return BatchInsertRenderer.withBatchInsertModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }
    
    public static <T> Builder<T> withRecords(List<T> records) {
        return new Builder<T>().withRecords(records);
    }
    
    public static class Builder<T> {
        private SqlTable table;
        private List<T> records = new ArrayList<>();
        private List<InsertMapping> columnMappings = new ArrayList<>();
        
        public Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder<T> withRecords(List<T> records) {
            this.records.addAll(records);
            return this;
        }
        
        public Builder<T> withColumnMappings(List<InsertMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return this;
        }
        
        public BatchInsertModel<T> build() {
            return new BatchInsertModel<>(this);
        }
    }
}
