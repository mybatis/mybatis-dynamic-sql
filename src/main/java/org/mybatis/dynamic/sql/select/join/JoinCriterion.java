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
package org.mybatis.dynamic.sql.select.join;

import java.util.Objects;

import org.mybatis.dynamic.sql.SqlColumn;

public class JoinCriterion<T> {

    private String connector;
    private SqlColumn<T> leftColumn;
    private JoinCondition<T> joinCondition;
    
    private JoinCriterion(Builder<T> builder) {
        connector = Objects.requireNonNull(builder.connector);
        leftColumn = Objects.requireNonNull(builder.joinColumn);
        joinCondition = Objects.requireNonNull(builder.joinCondition);
    }

    public String connector() {
        return connector;
    }
    
    public SqlColumn<T> leftColumn() {
        return leftColumn;
    }
    
    public SqlColumn<T> rightColumn() {
        return joinCondition.rightColumn();
    }
    
    public String operator() {
        return joinCondition.operator();
    }
    
    public static class Builder<T> {
        private SqlColumn<T> joinColumn;
        private JoinCondition<T> joinCondition;
        private String connector;
        
        public Builder<T> withJoinColumn(SqlColumn<T> joinColumn) {
            this.joinColumn = joinColumn;
            return this;
        }
        
        public Builder<T> withJoinCondition(JoinCondition<T> joinCondition) {
            this.joinCondition = joinCondition;
            return this;
        }
        
        public Builder<T> withConnector(String connector) {
            this.connector = connector;
            return this;
        }
        
        public JoinCriterion<T> build() {
            return new JoinCriterion<>(this);
        }
    }
}
