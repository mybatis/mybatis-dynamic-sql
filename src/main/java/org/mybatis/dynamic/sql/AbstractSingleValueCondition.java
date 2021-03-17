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
package org.mybatis.dynamic.sql;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class AbstractSingleValueCondition<T, S extends AbstractSingleValueCondition<T, S>>
        implements VisitableCondition<T> {
    protected final T value;

    protected AbstractSingleValueCondition(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    @Override
    public <R> R accept(ConditionVisitor<T, R> visitor) {
        return visitor.visit(this);
    }

    protected S filter(Predicate<T> predicate, Supplier<S> empty, S self) {
        if (shouldRender()) {
            return predicate.test(value) ? self : empty.get();
        } else {
            return self;
        }
    }

    /**
     * If renderable and the value matches the predicate, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @param predicate predicate applied to the value, if renderable
     * @return this condition if renderable and the value matches the predicate, otherwise a condition
     *     that will not render.
     */
    public abstract S filter(Predicate<T> predicate);

    protected S map(UnaryOperator<T> mapper, Function<T, S> constructor, S self) {
        if (shouldRender()) {
            return constructor.apply(mapper.apply(value));
        } else {
            return self;
        }
    }

    /**
     * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
     *     condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the value, if renderable
     * @return a new condition with the result of applying the mapper to the value of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    public abstract S map(UnaryOperator<T> mapper);

    public abstract String renderCondition(String columnName, String placeholder);
}
