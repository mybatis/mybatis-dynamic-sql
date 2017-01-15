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

import java.util.Arrays;
import java.util.stream.Stream;

public class SqlCriterion<T> extends AbstractCriterion<T, SqlColumn<T>, SqlCriterion<?>> {
    
    private SqlCriterion(Stream<SqlCriterion<?>> subCriteria) {
        super(subCriteria);
    }
    
    public static <T> SqlCriterion<T> of(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of(null,  column, condition, subCriteria);
    }
    
    public static <T> SqlCriterion<T> of(String connector, SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of(connector, column, condition, Arrays.stream(subCriteria));
    }

    public static <T> SqlCriterion<T> of(String connector, SqlColumn<T> column, Condition<T> condition, Stream<SqlCriterion<?>> subCriteria) {
        SqlCriterion<T> criterion = new SqlCriterion<>(subCriteria);
        criterion.column = column;
        criterion.condition = condition;
        criterion.connector = connector;
        return criterion;
    }
}
