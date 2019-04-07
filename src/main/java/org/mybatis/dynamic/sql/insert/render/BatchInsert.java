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
package org.mybatis.dynamic.sql.insert.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BatchInsert<T> {
    private String insertStatement;
    private List<T> records;
    
    private BatchInsert(Builder<T> builder) {
        insertStatement = Objects.requireNonNull(builder.insertStatement);
        records = Collections.unmodifiableList(Objects.requireNonNull(builder.records));
    }
    
    /**
     * Returns a list of InsertStatement objects.  This is useful for MyBatis batch support.
     * 
     * @return a List of InsertStatements
     */
    public List<InsertStatementProvider<T>> insertStatements() {
        return records.stream()
                .map(this::toInsertStatement)
                .collect(Collectors.toList());
    }
    
    private InsertStatementProvider<T> toInsertStatement(T record) {
        return DefaultInsertStatementProvider.withRecord(record)
                .withInsertStatement(insertStatement)
                .build();
    }

    /**
     * Returns the generated SQL for this batch.  This is useful for Spring JDBC batch support.
     * 
     * @return the generated INSERT statement
     */
    public String getInsertStatementSQL() {
        return insertStatement;
    }
    
    public static <T> Builder<T> withRecords(List<T> records) {
        return new Builder<T>().withRecords(records);
    }
    
    public static class Builder<T> {
        private String insertStatement;
        private List<T> records = new ArrayList<>();
        
        public Builder<T> withInsertStatement(String insertStatement) {
            this.insertStatement = insertStatement;
            return this;
        }

        public Builder<T> withRecords(List<T> records) {
            this.records.addAll(records);
            return this;
        }
        
        public BatchInsert<T> build() {
            return new BatchInsert<>(this);
        }
    }
}
