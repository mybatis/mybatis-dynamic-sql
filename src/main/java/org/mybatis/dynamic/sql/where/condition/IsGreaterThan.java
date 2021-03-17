/*
 *    Copyright 2016-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.where.condition;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.AbstractSingleValueCondition;

public class IsGreaterThan<T> extends AbstractSingleValueCondition<T, IsGreaterThan<T>> {
    private static final IsGreaterThan<?> EMPTY = new IsGreaterThan<Object>(null) {
        @Override
        public boolean shouldRender() {
            return false;
        }
    };

    public static <T> IsGreaterThan<T> empty() {
        @SuppressWarnings("unchecked")
        IsGreaterThan<T> t = (IsGreaterThan<T>) EMPTY;
        return t;
    }

    protected IsGreaterThan(T value) {
        super(value);
    }

    @Override
    public String renderCondition(String columnName, String placeholder) {
        return columnName + " > " + placeholder; //$NON-NLS-1$
    }

    public static <T> IsGreaterThan<T> of(T value) {
        return new IsGreaterThan<>(value);
    }

    /**
     * If renderable and the value matches the predicate, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @deprecated replaced by {@link IsGreaterThan#filter(Predicate)}
     * @param predicate predicate applied to the value, if renderable
     * @return this condition if renderable and the value matches the predicate, otherwise a condition
     *     that will not render.
     */
    @Deprecated
    public IsGreaterThan<T> when(Predicate<T> predicate) {
        return filter(predicate);
    }

    /**
     * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
     *     condition that will not render (this).
     *
     * @deprecated replaced by {@link IsGreaterThan#map(UnaryOperator)}
     * @param mapper a mapping function to apply to the value, if renderable
     * @return a new condition with the result of applying the mapper to the value of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    @Deprecated
    public IsGreaterThan<T> then(UnaryOperator<T> mapper) {
        return map(mapper);
    }

    @Override
    public IsGreaterThan<T> filter(Predicate<T> predicate) {
        return filter(predicate, IsGreaterThan::empty, this);
    }

    @Override
    public IsGreaterThan<T> map(UnaryOperator<T> mapper) {
        return map(mapper, IsGreaterThan::new, this);
    }
}
