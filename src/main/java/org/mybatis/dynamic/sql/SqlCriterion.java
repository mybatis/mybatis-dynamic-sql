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
package org.mybatis.dynamic.sql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SqlCriterion<T> {
    
    private SqlColumn<T> column;
    private VisitableCondition<T> condition;
    private Optional<String> connector;
    private Optional<List<SqlCriterion<?>>> subCriteria;
    
    private SqlCriterion(Builder<T> builder) {
        connector = Optional.ofNullable(builder.connector);
        column = builder.column;
        condition = builder.condition;
        subCriteria = Optional.ofNullable(builder.subCriteria);
    }
    
    public Optional<String> connector() {
        return connector;
    }
    
    public SqlColumn<T> column() {
        return column;
    }
    
    public VisitableCondition<T> condition() {
        return condition;
    }
    
    public Optional<Stream<SqlCriterion<?>>> subCriteria() {
        return subCriteria.map(List::stream);
    }

    public static class Builder<T> {
        private String connector;
        private SqlColumn<T> column;
        private VisitableCondition<T> condition;
        private List<SqlCriterion<?>> subCriteria;
        
        public Builder<T> withConnector(String connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder<T> withColumn(SqlColumn<T> column) {
            this.column = column;
            return this;
        }
        
        public Builder<T> withCondition(VisitableCondition<T> condition) {
            this.condition = condition;
            return this;
        }
        
        public Builder<T> withSubCriteria(List<SqlCriterion<?>> subCriteria) {
            this.subCriteria = subCriteria;
            return this;
        }
        
        public SqlCriterion<T> build() {
            return new SqlCriterion<>(this);
        }
    }
}
