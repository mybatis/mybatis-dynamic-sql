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

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;

public class IsNotBetween<T> extends AbstractTwoValueCondition<T> {
    private static final IsNotBetween<?> EMPTY = new IsNotBetween<Object>(-1, -1) {
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

    public static <T> IsNotBetween<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotBetween<T> t = (IsNotBetween<T>) EMPTY;
        return t;
    }

    protected IsNotBetween(T value1, T value2) {
        super(value1, value2);
    }

    @Override
    public String operator1() {
        return "not between"; //$NON-NLS-1$
    }

    @Override
    public String operator2() {
        return "and"; //$NON-NLS-1$
    }

    @Override
    public IsNotBetween<T> filter(BiPredicate<? super T, ? super T> predicate) {
        return filterSupport(predicate, IsNotBetween::empty, this);
    }

    @Override
    public IsNotBetween<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsNotBetween::empty, this);
    }

    @Override
    public <R> IsNotBetween<R> map(Function<? super T, ? extends R> mapper1,
            Function<? super T, ? extends R> mapper2) {
        return mapSupport(mapper1, mapper2, IsNotBetween::new, IsNotBetween::empty);
    }

    @Override
    public <R> IsNotBetween<R> map(Function<? super T, ? extends R> mapper) {
        return map(mapper, mapper);
    }

    public static <T> Builder<T> isNotBetween(T value1) {
        return new Builder<>(value1);
    }

    public static <T> WhenPresentBuilder<T> isNotBetweenWhenPresent(@Nullable T value1) {
        return new WhenPresentBuilder<>(value1);
    }

    public static class Builder<T> extends AndGatherer<T, IsNotBetween<T>> {

        private Builder(T value1) {
            super(value1);
        }

        @Override
        protected IsNotBetween<T> build(T value2) {
            return new IsNotBetween<>(value1, value2);
        }
    }

    public static class WhenPresentBuilder<T> extends AndWhenPresentGatherer<T, IsNotBetween<T>> {

        private WhenPresentBuilder(@Nullable T value1) {
            super(value1);
        }

        @Override
        protected IsNotBetween<T> build(@Nullable T value2) {
            if (value1 == null || value2 == null) {
                return empty();
            } else {
                return new IsNotBetween<>(value1, value2);
            }
        }
    }
}
