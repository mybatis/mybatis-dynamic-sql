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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class IsNotInCaseInsensitiveWhenPresent<T> extends AbstractListValueCondition<T>
        implements CaseInsensitiveRenderableCondition<T>, AbstractListValueCondition.Filterable<T>,
        AbstractListValueCondition.Mappable<T> {
    private static final IsNotInCaseInsensitiveWhenPresent<?> EMPTY =
            new IsNotInCaseInsensitiveWhenPresent<>(Collections.emptyList());

    public static <T> IsNotInCaseInsensitiveWhenPresent<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotInCaseInsensitiveWhenPresent<T> t = (IsNotInCaseInsensitiveWhenPresent<T>) EMPTY;
        return t;
    }

    protected IsNotInCaseInsensitiveWhenPresent(Collection<T> values) {
        super(values.stream().filter(Objects::nonNull).map(StringUtilities::upperCaseIfPossible).toList());
    }

    @Override
    public String operator() {
        return "not in"; //$NON-NLS-1$
    }

    @Override
    public IsNotInCaseInsensitiveWhenPresent<T> filter(Predicate<? super @NonNull T> predicate) {
        return filterSupport(predicate, IsNotInCaseInsensitiveWhenPresent::new,
                this, IsNotInCaseInsensitiveWhenPresent::empty);
    }

    @Override
    public <R> IsNotInCaseInsensitiveWhenPresent<R> map(Function<? super @NonNull T, ? extends @Nullable R> mapper) {
        return mapSupport(mapper, IsNotInCaseInsensitiveWhenPresent::new, IsNotInCaseInsensitiveWhenPresent::empty);
    }

    @SafeVarargs
    public static <T> IsNotInCaseInsensitiveWhenPresent<T> of(@Nullable T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsNotInCaseInsensitiveWhenPresent<T> of(@Nullable Collection<@Nullable T> values) {
        if (values == null) {
            return empty();
        } else {
            return new IsNotInCaseInsensitiveWhenPresent<>(values);
        }
    }
}
