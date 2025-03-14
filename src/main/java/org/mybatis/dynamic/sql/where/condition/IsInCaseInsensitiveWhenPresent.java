/*
 *    Copyright 2016-2025 the original author or authors.
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
import java.util.Objects;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class IsInCaseInsensitiveWhenPresent<T> extends AbstractListValueCondition<T>
        implements CaseInsensitiveRenderableCondition<T>, AbstractListValueCondition.Filterable<T>,
        AbstractListValueCondition.Mappable<T> {
    private static final IsInCaseInsensitiveWhenPresent<?> EMPTY =
            new IsInCaseInsensitiveWhenPresent<>(Collections.emptyList());

    public static <T> IsInCaseInsensitiveWhenPresent<T> empty() {
        @SuppressWarnings("unchecked")
        IsInCaseInsensitiveWhenPresent<T> t = (IsInCaseInsensitiveWhenPresent<T>) EMPTY;
        return t;
    }

    protected IsInCaseInsensitiveWhenPresent(Collection<T> values) {
        super(values);
    }

    @Override
    public String operator() {
        return "in"; //$NON-NLS-1$
    }

    @Override
    public IsInCaseInsensitiveWhenPresent<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsInCaseInsensitiveWhenPresent::new, this,
                IsInCaseInsensitiveWhenPresent::empty);
    }

    /**
     * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
     * condition that will not render (this).
     *
     * <p>This function DOES NOT automatically transform values to uppercase, so it potentially creates a
     * case-sensitive query. For String conditions you can use {@link StringUtilities#mapToUpperCase(Function)}
     * to add an uppercase transform after your mapping function.
     *
     * @param mapper a mapping function to apply to the value, if renderable
     * @param <R> type of the new condition
     * @return a new condition with the result of applying the mapper to the value of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    @Override
    public <R> IsInCaseInsensitiveWhenPresent<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, IsInCaseInsensitiveWhenPresent::new, IsInCaseInsensitiveWhenPresent::empty);
    }

    public static IsInCaseInsensitiveWhenPresent<String> of(@Nullable String... values) {
        return of(Arrays.asList(values));
    }

    public static IsInCaseInsensitiveWhenPresent<String> of(Collection<@Nullable String> values) {
        return new IsInCaseInsensitiveWhenPresent<>(
                values.stream().filter(Objects::nonNull).map(String::toUpperCase).toList());
    }
}
