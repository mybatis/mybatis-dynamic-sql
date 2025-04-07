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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;

public class IsBetweenWhenPresent<T> extends AbstractTwoValueCondition<T>
        implements AbstractTwoValueCondition.Filterable<T>, AbstractTwoValueCondition.Mappable<T> {
    private static final IsBetweenWhenPresent<?> EMPTY = new IsBetweenWhenPresent<Object>(-1, -1) {
        @Override
        public Object value1() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public Object value2() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsBetweenWhenPresent<T> empty() {
        @SuppressWarnings("unchecked")
        IsBetweenWhenPresent<T> t = (IsBetweenWhenPresent<T>) EMPTY;
        return t;
    }

    protected IsBetweenWhenPresent(T value1, T value2) {
        super(value1, value2);
    }

    @Override
    public String operator1() {
        return "between"; //$NON-NLS-1$
    }

    @Override
    public String operator2() {
        return "and"; //$NON-NLS-1$
    }

    @Override
    public IsBetweenWhenPresent<T> filter(BiPredicate<? super @NonNull T, ? super @NonNull T> predicate) {
        return filterSupport(predicate, IsBetweenWhenPresent::empty, this);
    }

    @Override
    public IsBetweenWhenPresent<T> filter(Predicate<? super @NonNull T> predicate) {
        return filterSupport(predicate, IsBetweenWhenPresent::empty, this);
    }

    @Override
    public <R> IsBetweenWhenPresent<R> map(Function<? super @NonNull T, ? extends @Nullable R> mapper1,
                                           Function<? super @NonNull T, ? extends @Nullable R> mapper2) {
        return mapSupport(mapper1, mapper2, IsBetweenWhenPresent::of, IsBetweenWhenPresent::empty);
    }

    @Override
    public <R> IsBetweenWhenPresent<R> map(Function<? super @NonNull T, ? extends @Nullable R> mapper) {
        return map(mapper, mapper);
    }

    public static <T> IsBetweenWhenPresent<T> of(@Nullable T value1, @Nullable T value2) {
        if (value1 == null || value2 == null) {
            return empty();
        } else {
            return new IsBetweenWhenPresent<>(value1, value2);
        }
    }

    public static <T> Builder<T> isBetweenWhenPresent(@Nullable T value1) {
        return new Builder<>(value1);
    }

    public static class Builder<T> extends AndWhenPresentGatherer<T, IsBetweenWhenPresent<T>> {
        private Builder(@Nullable T value1) {
            super(value1);
        }

        @Override
        protected IsBetweenWhenPresent<T> build(@Nullable T value2) {
            return IsBetweenWhenPresent.of(value1, value2);
        }
    }
}
