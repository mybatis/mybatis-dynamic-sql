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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;

public class JoinSpecification {

    private SqlTable table;
    private List<JoinCondition<?>> joinConditions = new ArrayList<>();
    
    private JoinSpecification(Builder builder) {
        table = builder.table;
        joinConditions.addAll(builder.joinConditions);
    }
    
    public SqlTable table() {
        return table;
    }
    
    public Stream<JoinCondition<?>> joinConditions() {
        return joinConditions.stream();
    }
    
    public static class Builder {
        private SqlTable table;
        private List<JoinCondition<?>> joinConditions = new ArrayList<>();
        
        public Builder(SqlTable table, List<JoinCondition<?>> joinConditions) {
            this.table = table;
            this.joinConditions.addAll(joinConditions);
        }
        
        public JoinSpecification build() {
            return new JoinSpecification(this);
        }
    }
}
