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
        super(values.stream().filter(Objects::nonNull).map(StringUtilities::upperCaseIfPossible).toList());
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

    @Override
    public <R> IsInCaseInsensitiveWhenPresent<R> map(Function<? super T, ? extends @Nullable R> mapper) {
        return mapSupport(mapper, IsInCaseInsensitiveWhenPresent::new, IsInCaseInsensitiveWhenPresent::empty);
    }

    @SafeVarargs
    public static <T> IsInCaseInsensitiveWhenPresent<T> of(@Nullable T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsInCaseInsensitiveWhenPresent<T> of(@Nullable Collection<@Nullable T> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        } else {
            return new IsInCaseInsensitiveWhenPresent<>(values);
        }
    }
}
