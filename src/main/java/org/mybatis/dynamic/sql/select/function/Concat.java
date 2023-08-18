/*
 *    Copyright 2016-2023 the original author or authors.
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
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class Concat<T> extends AbstractUniTypeFunction<T, Concat<T>> {
    private final List<BasicColumn> allColumns = new ArrayList<>();

    protected Concat(BindableColumn<T> firstColumn, List<BasicColumn> subsequentColumns) {
        super(firstColumn);
        allColumns.add(firstColumn);
        this.allColumns.addAll(subsequentColumns);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        // note - the cast below is added for type inference issues in some compilers
        FragmentCollector fc = allColumns.stream()
                .map(column -> column.render(renderingContext))
                .collect(FragmentCollector.collect());

        String fragment = fc.fragments()
                .collect(Collectors.joining(", ", "concat(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return FragmentAndParameters.withFragment(fragment)
                .withParameters(fc.parameters())
                .build();
    }

    @Override
    protected Concat<T> copy() {
        return new Concat<>(column, allColumns.subList(1, allColumns.size()));
    }

    public static <T> Concat<T> concat(BindableColumn<T> firstColumn, BasicColumn... subsequentColumns) {
        return new Concat<>(firstColumn, Arrays.asList(subsequentColumns));
    }

    public static <T> Concat<T> of(BindableColumn<T> firstColumn, List<BasicColumn> subsequentColumns) {
        return new Concat<>(firstColumn, subsequentColumns);
    }
}
