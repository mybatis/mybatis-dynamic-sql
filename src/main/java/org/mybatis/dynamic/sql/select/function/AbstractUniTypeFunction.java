/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.select.function;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;

/**
 * Represents a function that does not change the underlying data type.
 *
 * @author Jeff Butler
 *
 * @param <T>
 *            The type of the underlying column
 * @param <U>
 *            the specific subtype that implements the function
 */
public abstract class AbstractUniTypeFunction<T, U extends AbstractUniTypeFunction<T, U>>
        extends AbstractTypeConvertingFunction<T, T, U> {

    protected AbstractUniTypeFunction(BindableColumn<T> column) {
        super(column);
    }

    @Override
    public Optional<JDBCType> jdbcType() {
        return column.jdbcType();
    }

    @Override
    public Optional<String> typeHandler() {
        return column.typeHandler();
    }
}
