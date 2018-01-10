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

import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class Multiply<T extends Number> extends BaseMultipleColumnFunction<T> {
    
    private Multiply(List<BindableColumn<T>> columns) {
        super(columns);
    }
    
    private Multiply(BindableColumn<T> column, T number) {
        super(column, number);
    }
    
    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        if (number != null) {
            return "(" + columns.get(0).renderWithTableAlias(tableAliasCalculator) + " * " + number + ")";
        }
        return columns.stream()
                .map(column -> column.renderWithTableAlias(tableAliasCalculator))
                .collect(Collectors.joining(" * ", "(", ")"));
    }
    
    public static <T extends Number> Multiply<T> of(List<BindableColumn<T>> columns) {
        return new Multiply<>(columns);
    }
    
    public static <T extends Number> Multiply<T> of(BindableColumn<T> column, T value) {
        return new Multiply<>(column, value);
    }
    
    @Override
    protected Multiply<T> copyWithColumn(List<BindableColumn<T>> columns) {
        if (number != null) {
            return new Multiply<>(columns.get(0), number);    
        }
        return new Multiply<>(columns);
    }
}
