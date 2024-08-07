/*
 *    Copyright 2016-2024 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class OperatorFunction<T> extends AbstractUniTypeFunction<T, OperatorFunction<T>> {

    protected final BasicColumn secondColumn;
    protected final List<BasicColumn> subsequentColumns = new ArrayList<>();
    private final String operator;

    protected OperatorFunction(String operator, BasicColumn firstColumn, BasicColumn secondColumn,
            List<BasicColumn> subsequentColumns) {
        super(firstColumn);
        this.secondColumn = Objects.requireNonNull(secondColumn);
        this.subsequentColumns.addAll(subsequentColumns);
        this.operator = Objects.requireNonNull(operator);
    }

    @Override
    protected OperatorFunction<T> copy() {
        return new OperatorFunction<>(operator, column, secondColumn, subsequentColumns);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        String paddedOperator = " " + operator + " "; //$NON-NLS-1$ //$NON-NLS-2$

        return Stream.of(Stream.of(column, secondColumn), subsequentColumns.stream())
                .flatMap(Function.identity())
                .map(column -> column.render(renderingContext))
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(paddedOperator, "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static <T> OperatorFunction<T> of(String operator, BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return of(operator, firstColumn, secondColumn, Arrays.asList(subsequentColumns));
    }

    public static <T> OperatorFunction<T> of(String operator, BindableColumn<T> firstColumn, BasicColumn secondColumn,
            List<BasicColumn> subsequentColumns) {
        return new OperatorFunction<>(operator, firstColumn, secondColumn, subsequentColumns);
    }
}
