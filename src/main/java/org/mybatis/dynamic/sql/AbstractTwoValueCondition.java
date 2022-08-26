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
package org.mybatis.dynamic.sql;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractTwoValueCondition<T>
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

    protected <S extends AbstractTwoValueCondition<T>> S filterSupport(BiPredicate<? super T, ? super T> predicate,
            Supplier<S> emptySupplier, S self) {
        if (shouldRender()) {
            return predicate.test(value1, value2) ? self : emptySupplier.get();
        } else {
            return self;
        }
    }

    protected <S extends AbstractTwoValueCondition<T>> S filterSupport(Predicate<? super T> predicate,
            Supplier<S> emptySupplier, S self) {
        return filterSupport((v1, v2) -> predicate.test(v1) && predicate.test(v2), emptySupplier, self);
    }

    protected <R, S extends AbstractTwoValueCondition<R>> S mapSupport(Function<? super T, ? extends R> mapper1,
            Function<? super T, ? extends R> mapper2, BiFunction<R, R, S> constructor, Supplier<S> emptySupplier) {
        if (shouldRender()) {
            return constructor.apply(mapper1.apply(value1), mapper2.apply(value2));
        } else {
            return emptySupplier.get();
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
    public abstract AbstractTwoValueCondition<T> filter(BiPredicate<? super T, ? super T> predicate);

    /**
     * If renderable and both values match the predicate, returns this condition. Else returns a condition
     *     that will not render. This function implements a short-circuiting test. If the
     *     first value does not match the predicate, then the second value will not be tested.
     *
     * @param predicate predicate applied to both values, if renderable
     * @return this condition if renderable and the values match the predicate, otherwise a condition
     *     that will not render.
     */
    public abstract AbstractTwoValueCondition<T> filter(Predicate<? super T> predicate);

    public abstract String renderCondition(String columnName, String placeholder1, String placeholder2);
}
