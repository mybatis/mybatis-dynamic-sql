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
package org.mybatis.dynamic.sql.insert.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.AbstractSqlProvider;

public class InsertBatchProvider<T> extends AbstractSqlProvider {
    
    private String columnsPhrase;
    private String valuesPhrase;
    private List<T> records;
    
    private InsertBatchProvider(Builder<T> builder) {
        super(builder.tableName);
        this.columnsPhrase = Objects.requireNonNull(builder.columnsPhrase);
        this.valuesPhrase = Objects.requireNonNull(builder.valuesPhrase);
        this.records = Collections.unmodifiableList(Objects.requireNonNull(builder.records));
    }
    
    public List<InsertProvider<T>> insertProviders() {
        return records.stream()
                .map(this::toInsertProvider)
                .collect(Collectors.toList());
    }
    
    private InsertProvider<T> toInsertProvider(T record) {
        return new InsertProvider.Builder<T>()
                .withTableName(super.tableName())
                .withColumnsPhrase(columnsPhrase)
                .withValuesPhrase(valuesPhrase)
                .withRecord(record)
                .build();
    }

    public static class Builder<T> {
        private String tableName;
        private String columnsPhrase;
        private String valuesPhrase;
        private List<T> records = new ArrayList<>();
        
        public Builder<T> withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder<T> withColumnsPhrase(String columnsPhrase) {
            this.columnsPhrase = columnsPhrase;
            return this;
        }
        
        public Builder<T> withValuesPhrase(String valuesPhrase) {
            this.valuesPhrase = valuesPhrase;
            return this;
        }
        
        public Builder<T> withRecords(List<T> records) {
            this.records.addAll(records);
            return this;
        }
        
        public InsertBatchProvider<T> build() {
            return new InsertBatchProvider<>(this);
        }
    }
}
