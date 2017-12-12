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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.InsertRenderer;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.InsertMapping;

public class InsertModel<T> {
    private SqlTable table;
    private T record;
    private List<InsertMapping> columnMappings;
    
    private InsertModel(Builder<T> builder) {
        table = Objects.requireNonNull(builder.table);
        record = Objects.requireNonNull(builder.record);
        columnMappings = Objects.requireNonNull(builder.columnMappings);
    }

    public <R> Stream<R> mapColumnMappings(Function<InsertMapping, R> mapper) {
        return columnMappings.stream().map(mapper);
    }
    
    public T record() {
        return record;
    }
    
    public SqlTable table() {
        return table;
    }
    
    public InsertStatementProvider<T> render(RenderingStrategy renderingStrategy) {
        return InsertRenderer.withInsertModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }
    
    public static <T> Builder<T> withRecord(T record) {
        return new Builder<T>().withRecord(record);
    }
    
    public static class Builder<T> {
        private SqlTable table;
        private T record;
        private List<InsertMapping> columnMappings = new ArrayList<>();
        
        public Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder<T> withRecord(T record) {
            this.record = record;
            return this;
        }
        
        public Builder<T> withColumnMappings(List<InsertMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return this;
        }
        
        public InsertModel<T> build() {
            return new InsertModel<>(this);
        }
    }
}
