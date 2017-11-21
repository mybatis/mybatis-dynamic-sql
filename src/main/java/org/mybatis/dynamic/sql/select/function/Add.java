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
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class Add<T extends Number> implements BindableColumn<T> {
    
    private Optional<String> alias = Optional.empty();
    private BindableColumn<T> column1;
    private BindableColumn<T> column2;
    
    private Add(BindableColumn<T> column1, BindableColumn<T> column2) {
        this.column1 = Objects.requireNonNull(column1);
        this.column2 = Objects.requireNonNull(column2);
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }

    @Override
    public String applyTableAliasToName(TableAliasCalculator tableAliasCalculator) {
        return column1.applyTableAliasToName(tableAliasCalculator)
                + " + " //$NON-NLS-1$
                + column2.applyTableAliasToName(tableAliasCalculator);
    }

    @Override
    public BindableColumn<T> as(String alias) {
        Add<T> newColumn = new Add<>(column1, column2);
        newColumn.alias = Optional.of(alias);
        return newColumn;
    }

    @Override
    public JDBCType jdbcType() {
        return column1.jdbcType();
    }

    @Override
    public Optional<String> typeHandler() {
        return column1.typeHandler();
    }

    public static <T extends Number> Add<T> of(BindableColumn<T> column1, BindableColumn<T> column2) {
        return new Add<>(column1, column2);
    }
}
