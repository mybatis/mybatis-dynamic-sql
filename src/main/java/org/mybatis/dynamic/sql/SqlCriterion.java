/**
 *    Copyright 2016-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class SqlCriterion<T> {
    
    private BindableColumn<T> column;
    private VisitableCondition<T> condition;
    private String connector;
    private List<SqlCriterion<?>> subCriteria;
    
    private SqlCriterion(Builder<T> builder) {
        connector = builder.connector;
        column = Objects.requireNonNull(builder.column);
        condition = Objects.requireNonNull(builder.condition);
        subCriteria = Objects.requireNonNull(builder.subCriteria);
    }
    
    public Optional<String> connector() {
        return Optional.ofNullable(connector);
    }
    
    public BindableColumn<T> column() {
        return column;
    }
    
    public VisitableCondition<T> condition() {
        return condition;
    }
    
    public <R> Stream<R> mapSubCriteria(Function<SqlCriterion<?>, R> mapper) {
        return subCriteria.stream().map(mapper);
    }
    
    public static <T> Builder<T> withColumn(BindableColumn<T> column) {
        return new Builder<T>().withColumn(column);
    }

    public static class Builder<T> {
        private String connector;
        private BindableColumn<T> column;
        private VisitableCondition<T> condition;
        private List<SqlCriterion<?>> subCriteria = new ArrayList<>();
        
        public Builder<T> withConnector(String connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }
        
        public Builder<T> withCondition(VisitableCondition<T> condition) {
            this.condition = condition;
            return this;
        }
        
        public Builder<T> withSubCriteria(List<SqlCriterion<?>> subCriteria) {
            this.subCriteria.addAll(subCriteria);
            return this;
        }
        
        public SqlCriterion<T> build() {
            return new SqlCriterion<>(this);
        }
    }
}
