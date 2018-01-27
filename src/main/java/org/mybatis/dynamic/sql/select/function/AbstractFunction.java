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
package org.mybatis.dynamic.sql.select.function;

import java.sql.JDBCType;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;

public abstract class AbstractFunction<T, U extends AbstractFunction<T, U>> implements BindableColumn<T> {
    protected BindableColumn<T> column;
    protected String alias;

    protected AbstractFunction(BindableColumn<T> column) {
        this.column = Objects.requireNonNull(column);
    }
    
    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public U as(String alias) {
        U newThing = copy();
        newThing.alias = alias;
        return newThing;
    }

    @Override
    public Optional<JDBCType> jdbcType() {
        return column.jdbcType();
    }

    @Override
    public Optional<String> typeHandler() {
        return column.typeHandler();
    }
    
    protected abstract U copy();
}
