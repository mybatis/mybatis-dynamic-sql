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

public class DefaultMultiRowInsertStatementProvider<T> implements MultiRowInsertStatementProvider<T> {
    
    private List<T> records;
    private String insertStatement;
    
    private DefaultMultiRowInsertStatementProvider(Builder<T> builder) {
        insertStatement = Objects.requireNonNull(builder.insertStatement);
        records = Collections.unmodifiableList(builder.records);
    }
    
    @Override
    public String getInsertStatement() {
        return insertStatement;
    }
    
    @Override
    public List<T> getRecords() {
        return records;
    }
    
    public static class Builder<T> {
        private List<T> records = new ArrayList<>();
        private String insertStatement;

        public Builder<T> withRecords(List<T> records) {
            this.records.addAll(records);
            return this;
        }
        
        public Builder<T> withInsertStatement(String insertStatement) {
            this.insertStatement = insertStatement;
            return this;
        }
        
        public DefaultMultiRowInsertStatementProvider<T> build() {
            return new DefaultMultiRowInsertStatementProvider<>(this);
        }
    }
}
