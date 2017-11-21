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
package org.mybatis.dynamic.sql.select.function;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;

public abstract class BaseFunction<T, S extends BaseFunction<T, S>> implements BindableColumn<T> {

    protected BindableColumn<T> column;
    
    protected BaseFunction(BindableColumn<T> column) {
        this.column = column;
    }

    @Override
    public S as(String alias) {
        return copyWithColumn(column.as(alias));
    }

    @Override
    public JDBCType jdbcType() {
        return column.jdbcType();
    }

    @Override
    public Optional<String> typeHandler() {
        return column.typeHandler();
    }

    @Override
    public Optional<String> alias() {
        return column.alias();
    }
    
    protected abstract S copyWithColumn(BindableColumn<T> column);
}
