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
package org.mybatis.dynamic.sql.select;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;

public class JoinSpecification {

    private SqlTable table;
    private JoinColumnAndCondition<?> firstJoinColumnAndCondition;
    private Optional<List<JoinColumnAndCondition<?>>> subsequentJoinColumnsAndConditions;
    
    private JoinSpecification() {
        super();
    }
    
    public SqlTable table() {
        return table;
    }
    
    public JoinColumnAndCondition<?> firstJoinColumnAndCondition() {
        return firstJoinColumnAndCondition;
    }
    
    public Optional<Stream<JoinColumnAndCondition<?>>> subsequentJoinColumnsAndConditions() {
        return subsequentJoinColumnsAndConditions.map(List::stream);
    }
    
    public static <T> JoinSpecification of(SqlTable table, JoinColumnAndCondition<T> joinColumnAndCondition) {
        JoinSpecification joinModel = new JoinSpecification();
        joinModel.table = table;
        joinModel.firstJoinColumnAndCondition = joinColumnAndCondition;
        joinModel.subsequentJoinColumnsAndConditions = Optional.empty();
        return joinModel;
    }

    public static <T> JoinSpecification of(SqlTable table, JoinColumnAndCondition<T> joinColumnAndCondition, JoinColumnAndCondition<?>...joinColumnsAndConditions) {
        JoinSpecification joinModel = new JoinSpecification();
        joinModel.table = table;
        joinModel.firstJoinColumnAndCondition = joinColumnAndCondition;
        joinModel.subsequentJoinColumnsAndConditions = Optional.of(Arrays.asList(joinColumnsAndConditions));
        return joinModel;
    }
}
