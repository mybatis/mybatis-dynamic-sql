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
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.util.Utilities;

public class IsNotInWhenPresent<T> extends AbstractListValueCondition<T>
        implements AbstractListValueCondition.Filterable<T>, AbstractListValueCondition.Mappable<T> {
    private static final IsNotInWhenPresent<?> EMPTY = new IsNotInWhenPresent<>(Collections.emptyList());

    public static <T> IsNotInWhenPresent<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotInWhenPresent<T> t = (IsNotInWhenPresent<T>) EMPTY;
        return t;
    }

    protected IsNotInWhenPresent(Collection<@Nullable T> values) {
        super(Utilities.filterNulls(values).toList());
    }

    @Override
    public String operator() {
        return "not in"; //$NON-NLS-1$
    }

    @Override
    public IsNotInWhenPresent<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsNotInWhenPresent::new, this, IsNotInWhenPresent::empty);
    }

    @Override
    public <R> IsNotInWhenPresent<R> map(Function<? super T, ? extends @Nullable R> mapper) {
        return mapSupport(mapper, IsNotInWhenPresent::new, IsNotInWhenPresent::empty);
    }

    @SafeVarargs
    public static <T> IsNotInWhenPresent<T> of(@Nullable T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsNotInWhenPresent<T> of(@Nullable Collection<@Nullable T> values) {
        if (values == null) {
            return empty();
        } else {
            return new IsNotInWhenPresent<>(values);
        }
    }
}
