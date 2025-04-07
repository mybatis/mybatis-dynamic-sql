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

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;

public class IsEqualToWhenPresent<T> extends AbstractSingleValueCondition<T>
        implements AbstractSingleValueCondition.Filterable<T>, AbstractSingleValueCondition.Mappable<T> {

    private static final IsEqualToWhenPresent<?> EMPTY = new IsEqualToWhenPresent<Object>(-1) {
        @Override
        public Object value() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsEqualToWhenPresent<T> empty() {
        @SuppressWarnings("unchecked")
        IsEqualToWhenPresent<T> t = (IsEqualToWhenPresent<T>) EMPTY;
        return t;
    }

    protected IsEqualToWhenPresent(T value) {
        super(value);
    }

    @Override
    public String operator() {
        return "="; //$NON-NLS-1$
    }

    public static <T> IsEqualToWhenPresent<T> of(@Nullable T value) {
        if (value == null) {
            return empty();
        } else {
            return new IsEqualToWhenPresent<>(value);
        }
    }

    @Override
    public IsEqualToWhenPresent<T> filter(Predicate<? super @NonNull T> predicate) {
        return filterSupport(predicate, IsEqualToWhenPresent::empty, this);
    }

    @Override
    public <R> IsEqualToWhenPresent<R> map(Function<? super @NonNull T, ? extends @Nullable R> mapper) {
        return mapSupport(mapper, IsEqualToWhenPresent::of, IsEqualToWhenPresent::empty);
    }
}
