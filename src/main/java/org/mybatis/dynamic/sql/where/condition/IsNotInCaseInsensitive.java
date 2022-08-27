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
import org.mybatis.dynamic.sql.Callback;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class IsNotInCaseInsensitive extends AbstractListValueCondition<String> {
    private static final IsNotInCaseInsensitive EMPTY = new IsNotInCaseInsensitive(Collections.emptyList());

    public static IsNotInCaseInsensitive empty() {
        return EMPTY;
    }

    /**
     * Build an empty condition.
     *
     * @return a new empty condition
     *
     * @deprecated in favor of the statement configuration functions
     */
    @Deprecated
    private IsNotInCaseInsensitive emptyWithCallback() {
        return new IsNotInCaseInsensitive(Collections.emptyList(), emptyCallback);
    }

    protected IsNotInCaseInsensitive(Collection<String> values) {
        super(values);
    }

    /**
     * Build a new instance with a callback.
     *
     * @param values
     *            values
     * @param emptyCallback
     *            empty callback
     *
     * @deprecated in favor of the statement configuration functions
     */
    @Deprecated
    protected IsNotInCaseInsensitive(Collection<String> values, Callback emptyCallback) {
        super(values, emptyCallback);
    }

    @Override
    public String renderCondition(String columnName, Stream<String> placeholders) {
        return "upper(" + columnName + ") " //$NON-NLS-1$ //$NON-NLS-2$
                + placeholders.collect(
                        Collectors.joining(",", "not in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Build a new instance with a callback.
     *
     * @param callback
     *            a callback function - typically throws an exception to block the statement from executing
     *
     * @return this condition
     *
     * @deprecated in favor of the statement configuration functions
     */
    @Deprecated
    @Override
    public IsNotInCaseInsensitive withListEmptyCallback(Callback callback) {
        return new IsNotInCaseInsensitive(values, callback);
    }

    @Override
    public IsNotInCaseInsensitive filter(Predicate<? super String> predicate) {
        return filterSupport(predicate, IsNotInCaseInsensitive::new, this, this::emptyWithCallback);
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
        return mapSupport(mapper, IsNotInCaseInsensitive::new, this::emptyWithCallback);
    }

    public static IsNotInCaseInsensitive of(String... values) {
        return of(Arrays.asList(values));
    }

    public static IsNotInCaseInsensitive of(Collection<String> values) {
        return new IsNotInCaseInsensitive(values).map(StringUtilities::safelyUpperCase);
    }
}
