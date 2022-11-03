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
package org.mybatis.dynamic.sql.where.condition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class IsInCaseInsensitive extends AbstractListValueCondition<String> {
    private static final IsInCaseInsensitive EMPTY = new IsInCaseInsensitive(Collections.emptyList());

    public static IsInCaseInsensitive empty() {
        return EMPTY;
    }

    protected IsInCaseInsensitive(Collection<String> values) {
        super(values);
    }

    @Override
    public String renderCondition(String columnName, Stream<String> placeholders) {
        return "upper(" + columnName + ") " //$NON-NLS-1$ //$NON-NLS-2$
                + placeholders.collect(
                        Collectors.joining(",", "in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public IsInCaseInsensitive filter(Predicate<? super String> predicate) {
        return filterSupport(predicate, IsInCaseInsensitive::new, this, IsInCaseInsensitive::empty);
    }

    /**
     * If renderable, apply the mapping to each value in the list return a new condition with the mapped values.
     *     Else return a condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the values, if renderable
     * @return a new condition with mapped values if renderable, otherwise a condition
     *     that will not render.
     */
    public IsInCaseInsensitive map(UnaryOperator<String> mapper) {
        return mapSupport(mapper, IsInCaseInsensitive::new, IsInCaseInsensitive::empty);
    }

    public static IsInCaseInsensitive of(String... values) {
        return of(Arrays.asList(values));
    }

    public static IsInCaseInsensitive of(Collection<String> values) {
        return new IsInCaseInsensitive(values).map(StringUtilities::safelyUpperCase);
    }
}
