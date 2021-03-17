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

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class AbstractTwoValueCondition<T, S extends AbstractTwoValueCondition<T, S>>
        implements VisitableCondition<T> {
    protected final T value1;
    protected final T value2;

    protected AbstractTwoValueCondition(T value1, T value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T value1() {
        return value1;
    }

    public T value2() {
        return value2;
    }

    @Override
    public <R> R accept(ConditionVisitor<T, R> visitor) {
        return visitor.visit(this);
    }

    protected S filter(BiPredicate<T, T> predicate, Supplier<S> empty, S self) {
        if (shouldRender()) {
            return predicate.test(value1, value2) ? self : empty.get();
        } else {
            return self;
        }
    }

    /**
     * If renderable and the values match the predicate, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @param predicate predicate applied to the values, if renderable
     * @return this condition if renderable and the values match the predicate, otherwise a condition
     *     that will not render.
     */
    public abstract S filter(BiPredicate<T, T> predicate);

    protected S map(UnaryOperator<T> mapper1, UnaryOperator<T> mapper2, BiFunction<T, T, S> constructor, S self) {
        if (shouldRender()) {
            return constructor.apply(mapper1.apply(value1), mapper2.apply(value2));
        } else {
            return self;
        }
    }

    /**
     * If renderable, apply the mappings to the values and return a new condition with the new values. Else return a
     *     condition that will not render (this).
     *
     * @param mapper1 a mapping function to apply to the first value, if renderable
     * @param mapper2 a mapping function to apply to the second value, if renderable
     * @return a new condition with the result of applying the mappers to the values of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    public abstract S map(UnaryOperator<T> mapper1, UnaryOperator<T> mapper2);

    public abstract String renderCondition(String columnName, String placeholder1, String placeholder2);
}
