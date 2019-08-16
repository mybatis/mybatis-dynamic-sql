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
package org.mybatis.dynamic.sql.select.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;

public class JoinSpecification {

    private SqlTable table;
    private List<JoinCriterion> joinCriteria;
    private JoinType joinType;
    
    private JoinSpecification(Builder builder) {
        table = Objects.requireNonNull(builder.table);
        joinCriteria = Objects.requireNonNull(builder.joinCriteria);
        joinType = Objects.requireNonNull(builder.joinType);
    }
    
    public SqlTable table() {
        return table;
    }
    
    public <R> Stream<R> mapJoinCriteria(Function<JoinCriterion, R> mapper) {
        return joinCriteria.stream().map(mapper);
    }
    
    public JoinType joinType() {
        return joinType;
    }
    
    public static Builder withJoinTable(SqlTable table) {
        return new Builder().withJoinTable(table);
    }
    
    public static class Builder {
        private SqlTable table;
        private List<JoinCriterion> joinCriteria = new ArrayList<>();
        private JoinType joinType;
        
        public Builder withJoinTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder withJoinCriterion(JoinCriterion joinCriterion) {
            this.joinCriteria.add(joinCriterion);
            return this;
        }
        
        public Builder withJoinCriteria(List<JoinCriterion> joinCriteria) {
            this.joinCriteria.addAll(joinCriteria);
            return this;
        }
        
        public Builder withJoinType(JoinType joinType) {
            this.joinType = joinType;
            return this;
        }
        
        public JoinSpecification build() {
            return new JoinSpecification(this);
        }
    }
}
