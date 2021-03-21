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

public abstract class AbstractSingleValueCondition<T> implements VisitableCondition<T> {
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

    protected <S> S filterSupport(Predicate<? super T> predicate, Supplier<S> empty, S self) {
        if (shouldRender()) {
            return predicate.test(value) ? self : empty.get();
        } else {
            return self;
        }
    }

    protected <R, S> S mapSupport(Function<? super T, ? extends R> mapper, Function<R, S> constructor,
            Supplier<S> empty) {
        if (shouldRender()) {
            return constructor.apply(mapper.apply(value));
        } else {
            return empty.get();
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
    public abstract AbstractSingleValueCondition<T> filter(Predicate<? super T> predicate);

    public abstract String renderCondition(String columnName, String placeholder);
}
