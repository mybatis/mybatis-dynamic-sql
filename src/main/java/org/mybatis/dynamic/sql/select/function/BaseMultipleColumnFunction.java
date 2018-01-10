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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;

public abstract class BaseMultipleColumnFunction<T, S extends BaseMultipleColumnFunction<T, S>> implements BindableColumn<T> {
    protected String alias;
    protected List<BindableColumn<T>> columns;
    protected BaseMultipleColumnFunction<T, S> otherOperation;
    
    protected BaseMultipleColumnFunction(List<BindableColumn<T>> columns) {
        this.columns = Objects.requireNonNull(columns);
    }
    
    protected BaseMultipleColumnFunction(List<BindableColumn<T>> columns, BaseMultipleColumnFunction<T, S> otherOperation) {
        this.columns = Objects.requireNonNull(columns);
        this.otherOperation = otherOperation;
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public BindableColumn<T> as(String alias) {
        BaseMultipleColumnFunction<T, S> newColumn = copyWithColumn(columns, otherOperation);
        newColumn.alias = alias;
        return newColumn;
    }

    @Override
    public JDBCType jdbcType() {
        return columns.get(0).jdbcType();
    }

    @Override
    public Optional<String> typeHandler() {
        return columns.get(0).typeHandler();
    }
    
    protected abstract BaseMultipleColumnFunction<T, S> copyWithColumn(List<BindableColumn<T>> columns, BaseMultipleColumnFunction<T, S> otherOperation);
}
