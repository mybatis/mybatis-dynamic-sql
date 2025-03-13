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

import org.mybatis.dynamic.sql.AbstractSingleValueCondition;

public class IsEqualTo<T> extends AbstractSingleValueCondition<T> {

    private static final IsEqualTo<?> EMPTY = new IsEqualTo<Object>(-1) {
        @Override
        public Object value() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsEqualTo<T> empty() {
        @SuppressWarnings("unchecked")
        IsEqualTo<T> t = (IsEqualTo<T>) EMPTY;
        return t;
    }

    protected IsEqualTo(T value) {
        super(value);
    }

    @Override
    public String operator() {
        return "="; //$NON-NLS-1$
    }

    public static <T> IsEqualTo<T> of(T value) {
        return new IsEqualTo<>(value);
    }

    @Override
    public IsEqualTo<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsEqualTo::empty, this);
    }

    @Override
    public <R> IsEqualTo<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, IsEqualTo::new, IsEqualTo::empty);
    }
}
