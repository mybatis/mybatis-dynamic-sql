/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.Criterion;

public class SqlCriterion<T> extends Criterion<T, SqlField<T>, SqlCriterion<?>> {
    
    private SqlCriterion() {
        super();
    }
    
    public String renderField(Function<SqlField<?>, String> nameFunction) {
        return condition.composeLeftSide(nameFunction.apply(field));
    }
    
    public static <T> SqlCriterion<T> of(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of(null,  field, condition, subCriteria);
    }
    
    public static <T> SqlCriterion<T> of(String connector, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of(connector, field, condition, Arrays.stream(subCriteria));
    }

    public static <T> SqlCriterion<T> of(String connector, SqlField<T> field, Condition<T> condition, Stream<SqlCriterion<?>> subCriteria) {
        SqlCriterion<T> criterion = new SqlCriterion<>();
        criterion.field = field;
        criterion.condition = condition;
        criterion.connector = connector;
        subCriteria.forEach(criterion.subCriteria::add);
        return criterion;
    }
}
