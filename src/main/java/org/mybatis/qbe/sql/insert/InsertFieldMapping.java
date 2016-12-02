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
package org.mybatis.qbe.sql.insert;

import java.util.function.Function;

import org.mybatis.qbe.sql.SqlField;

public class InsertFieldMapping<T, S> {

    private SqlField<S> field;
    private String property;
    private Function<T, S> getterFunction;

    private InsertFieldMapping() {
        super();
    }
    
    public SqlField<S> getField() {
        return field;
    }

    public String getProperty() {
        return property;
    }

    public Function<T, S> getGetterFunction() {
        return getterFunction;
    }

    public static <T, S> InsertFieldMapping<T, S> of(SqlField<S> field, String property, Function<T, S> getterFunction) {
        InsertFieldMapping<T, S> mapping = new InsertFieldMapping<>();
        mapping.field = field;
        mapping.property = property;
        mapping.getterFunction = getterFunction;
        return mapping;
    }
}
