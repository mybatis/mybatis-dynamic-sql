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
package org.mybatis.dynamic.sql.where.condition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

import org.mybatis.dynamic.sql.AbstractListValueCondition;

public class IsNotIn<T> extends AbstractListValueCondition<T> {
    private static final IsNotIn<?> EMPTY = new IsNotIn<>(Collections.emptyList());

    public static <T> IsNotIn<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotIn<T> t = (IsNotIn<T>) EMPTY;
        return t;
    }

    protected IsNotIn(Collection<T> values) {
        super(values);
    }

    @Override
    public String operator() {
        return "not in"; //$NON-NLS-1$
    }

    @Override
    public IsNotIn<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsNotIn::new, this, IsNotIn::empty);
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
    public <R> IsNotIn<R> map(Function<? super T, ? extends R> mapper) {
        Function<Collection<R>, IsNotIn<R>> constructor = IsNotIn::new;
        return mapSupport(mapper, constructor, IsNotIn::empty);
    }

    @SafeVarargs
    public static <T> IsNotIn<T> of(T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsNotIn<T> of(Collection<T> values) {
        return new IsNotIn<>(values);
    }
}
