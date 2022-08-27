/*
 *    Copyright 2016-2022 the original author or authors.
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

import java.util.function.Function;
import java.util.function.Predicate;

import org.mybatis.dynamic.sql.AbstractSingleValueCondition;

public class IsGreaterThanOrEqualTo<T> extends AbstractSingleValueCondition<T> {
    private static final IsGreaterThanOrEqualTo<?> EMPTY = new IsGreaterThanOrEqualTo<Object>(null) {
        @Override
        public boolean shouldRender() {
            return false;
        }
    };

    public static <T> IsGreaterThanOrEqualTo<T> empty() {
        @SuppressWarnings("unchecked")
        IsGreaterThanOrEqualTo<T> t = (IsGreaterThanOrEqualTo<T>) EMPTY;
        return t;
    }

    protected IsGreaterThanOrEqualTo(T value) {
        super(value);
    }

    @Override
    public String renderCondition(String columnName, String placeholder) {
        return columnName + " >= " + placeholder; //$NON-NLS-1$
    }

    public static <T> IsGreaterThanOrEqualTo<T> of(T value) {
        return new IsGreaterThanOrEqualTo<>(value);
    }

    @Override
    public IsGreaterThanOrEqualTo<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsGreaterThanOrEqualTo::empty, this);
    }

    /**
     * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
     * condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the value, if renderable
     * @param <R> type of the new condition
     * @return a new condition with the result of applying the mapper to the value of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    public <R> IsGreaterThanOrEqualTo<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, IsGreaterThanOrEqualTo::new, IsGreaterThanOrEqualTo::empty);
    }
}
