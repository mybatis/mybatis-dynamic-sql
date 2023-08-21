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
package org.mybatis.dynamic.sql.where.condition;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.AbstractListValueCondition;

public class IsIn<T> extends AbstractListValueCondition<T> {
    private static final IsIn<?> EMPTY = new IsIn<>(Collections.emptyList());

    public static <T> IsIn<T> empty() {
        @SuppressWarnings("unchecked")
        IsIn<T> t = (IsIn<T>) EMPTY;
        return t;
    }

    protected IsIn(Collection<T> values) {
        super(values);
    }

    @Override
    public String renderCondition(String columnName,
                                  Function<Collector<CharSequence, ?, String>, String> placeholderFunction) {
        return spaceAfter(columnName) + placeholderFunction.apply(
                Collectors.joining(",", "in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public IsIn<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsIn::new, this, IsIn::empty);
    }

    /**
     * If renderable, apply the mapping to each value in the list return a new condition with the mapped values.
     *     Else return a condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the values, if renderable
     * @param <R> type of the new condition
     * @return a new condition with mapped values if renderable, otherwise a condition
     *     that will not render.
     */
    public <R> IsIn<R> map(Function<? super T, ? extends R> mapper) {
        Function<Collection<R>, IsIn<R>> constructor = IsIn::new;
        return mapSupport(mapper, constructor, IsIn::empty);
    }

    @SafeVarargs
    public static <T> IsIn<T> of(T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsIn<T> of(Collection<T> values) {
        return new IsIn<>(values);
    }
}
