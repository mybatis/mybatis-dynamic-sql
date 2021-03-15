/*
 *    Copyright 2016-2021 the original author or authors.
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
package org.mybatis.dynamic.sql.where.condition;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.Callback;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class IsNotInCaseInsensitive extends AbstractListValueCondition<String, IsNotInCaseInsensitive> {

    protected IsNotInCaseInsensitive(Collection<String> values) {
        super(values);
    }

    protected IsNotInCaseInsensitive(Collection<String> values, Callback emptyCallback) {
        super(values, emptyCallback);
    }

    @Override
    public String renderCondition(String columnName, Stream<String> placeholders) {
        return "upper(" + columnName + ") " + //$NON-NLS-1$ //$NON-NLS-2$
                placeholders.collect(
                        Collectors.joining(",", "not in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public IsNotInCaseInsensitive withListEmptyCallback(Callback callback) {
        return new IsNotInCaseInsensitive(values, callback);
    }

    /**
     * If renderable apply the predicate to each value in the list and return a new condition with the filtered values.
     *     Else returns a condition that will not render (this). If all values are filtered out of the value
     *     list, then the condition will not render.
     *
     * @param predicate predicate applied to the values, if renderable
     * @return a new condition with filtered values if renderable, otherwise a condition
     *     that will not render.
     */
    public IsNotInCaseInsensitive filter(Predicate<String> predicate) {
        if (shouldRender()) {
            return new IsNotInCaseInsensitive(applyFilter(predicate), emptyCallback);
        } else {
            return this;
        }
    }

    /**
     * If renderable, apply the mapping to each value in the list return a new condition with the mapped values.
     *     Else return a condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the values, if renderable
     * @return a new condition with mapped values if renderable, otherwise a condition
     *     that will not render.
     */
    public IsNotInCaseInsensitive map(UnaryOperator<String> mapper) {
        if (shouldRender()) {
            return new IsNotInCaseInsensitive(applyMapper(mapper), emptyCallback);
        } else {
            return this;
        }
    }

    public static IsNotInCaseInsensitive of(String... values) {
        return of(Arrays.asList(values));
    }

    public static IsNotInCaseInsensitive of(Collection<String> values) {
        return new IsNotInCaseInsensitive(values).map(StringUtilities::safelyUpperCase);
    }
}
