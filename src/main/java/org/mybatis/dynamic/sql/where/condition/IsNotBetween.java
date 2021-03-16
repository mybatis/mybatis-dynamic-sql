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

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.util.Predicates;

public class IsNotBetween<T> extends AbstractTwoValueCondition<T> {

    protected IsNotBetween(Supplier<T> valueSupplier1, Supplier<T> valueSupplier2) {
        super(valueSupplier1, valueSupplier2);
    }

    @Override
    public String renderCondition(String columnName, String placeholder1, String placeholder2) {
        return columnName + " not between " + placeholder1 + " and " + placeholder2; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * If renderable and the values match the predicate, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @deprecated replaced by {@link IsNotBetween#filter(BiPredicate)}
     * @param predicate predicate applied to the values, if renderable
     * @return this condition if renderable and the values match the predicate, otherwise a condition
     *     that will not render.
     */
    @Deprecated
    public IsNotBetween<T> when(BiPredicate<T, T> predicate) {
        return filter(predicate);
    }

    /**
     * If renderable, apply the mappings to the values and return a new condition with the new values. Else return a
     *     condition that will not render (this).
     *
     * @deprecated replaced by {@link IsNotBetween#map(UnaryOperator, UnaryOperator)}
     * @param mapper1 a mapping function to apply to the first value, if renderable
     * @param mapper2 a mapping function to apply to the second value, if renderable
     * @return a new condition with the result of applying the mappers to the values of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    @Deprecated
    public IsNotBetween<T> then(UnaryOperator<T> mapper1, UnaryOperator<T> mapper2) {
        return map(mapper1, mapper2);
    }

    /**
     * If renderable and the values match the predicate, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @param predicate predicate applied to the values, if renderable
     * @return this condition if renderable and the values match the predicate, otherwise a condition
     *     that will not render.
     */
    public IsNotBetween<T> filter(BiPredicate<T, T> predicate) {
        if (shouldRender()) {
            return predicate.test(value1(), value2()) ? this : EmptyIsNotBetween.empty();
        } else {
            return this;
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
    public IsNotBetween<T> map(UnaryOperator<T> mapper1, UnaryOperator<T> mapper2) {
        return shouldRender() ? new IsNotBetween<>(() -> mapper1.apply(value1()), () -> mapper2.apply(value2())) : this;
    }

    public static <T> Builder<T> isNotBetween(Supplier<T> valueSupplier1) {
        return new Builder<>(valueSupplier1);
    }

    public static <T> WhenPresentBuilder<T> isNotBetweenWhenPresent(Supplier<T> valueSupplier1) {
        return new WhenPresentBuilder<>(valueSupplier1);
    }

    public static class Builder<T> extends AndGatherer<T, IsNotBetween<T>> {

        private Builder(Supplier<T> valueSupplier1) {
            super(valueSupplier1);
        }

        @Override
        protected IsNotBetween<T> build() {
            return new IsNotBetween<>(valueSupplier1, valueSupplier2);
        }
    }

    public static class WhenPresentBuilder<T> extends AndGatherer<T, IsNotBetween<T>> {
        private WhenPresentBuilder(Supplier<T> valueSupplier1) {
            super(valueSupplier1);
        }

        @Override
        protected IsNotBetween<T> build() {
            return new IsNotBetween<>(valueSupplier1, valueSupplier2).filter(Predicates.bothPresent());
        }
    }

    public static class EmptyIsNotBetween<T> extends IsNotBetween<T> {
        private static final EmptyIsNotBetween<?> EMPTY = new EmptyIsNotBetween<>();

        public static <T> EmptyIsNotBetween<T> empty() {
            @SuppressWarnings("unchecked")
            EmptyIsNotBetween<T> t = (EmptyIsNotBetween<T>) EMPTY;
            return t;
        }

        public EmptyIsNotBetween() {
            super(() -> null, () -> null);
        }

        @Override
        public boolean shouldRender() {
            return false;
        }
    }
}
