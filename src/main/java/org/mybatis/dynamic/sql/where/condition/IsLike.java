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
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;

public class IsLike<T> extends AbstractSingleValueCondition<T>
        implements AbstractSingleValueCondition.Filterable<T>, AbstractSingleValueCondition.Mappable<T> {

    private static final IsLike<?> EMPTY = new IsLike<Object>(-1) {
        @Override
        public Object value() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsLike<T> empty() {
        @SuppressWarnings("unchecked")
        IsLike<T> t = (IsLike<T>) EMPTY;
        return t;
    }

    protected IsLike(T value) {
        super(value);
    }

    @Override
    public String operator() {
        return "like"; //$NON-NLS-1$
    }

    public static <T> IsLike<T> of(T value) {
        return new IsLike<>(value);
    }

    @Override
    public IsLike<T> filter(Predicate<? super @NonNull T> predicate) {
        return filterSupport(predicate, IsLike::empty, this);
    }

    @Override
    public <R> IsLike<R> map(Function<? super @NonNull T, ? extends @NonNull R> mapper) {
        return mapSupport(mapper, IsLike::new, IsLike::empty);
    }
}
