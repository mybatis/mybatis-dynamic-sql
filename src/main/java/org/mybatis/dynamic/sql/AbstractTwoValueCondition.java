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
package org.mybatis.dynamic.sql;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public abstract class AbstractTwoValueCondition<T> implements RenderableCondition<T> {
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

    protected <S extends AbstractTwoValueCondition<T>> S filterSupport(BiPredicate<? super T, ? super T> predicate,
            Supplier<S> emptySupplier, S self) {
        if (isEmpty()) {
            return self;
        } else {
            return predicate.test(value1, value2) ? self : emptySupplier.get();
        }
    }

    protected <S extends AbstractTwoValueCondition<T>> S filterSupport(Predicate<? super T> predicate,
            Supplier<S> emptySupplier, S self) {
        return filterSupport((v1, v2) -> predicate.test(v1) && predicate.test(v2), emptySupplier, self);
    }

    protected <R, S extends AbstractTwoValueCondition<R>> S mapSupport(Function<? super T, ? extends R> mapper1,
            Function<? super T, ? extends R> mapper2, BiFunction<R, R, S> constructor, Supplier<S> emptySupplier) {
        if (isEmpty()) {
            return emptySupplier.get();
        } else {
            return constructor.apply(mapper1.apply(value1), mapper2.apply(value2));
        }
    }

    public abstract String operator1();

    public abstract String operator2();

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        RenderedParameterInfo parameterInfo1 = renderingContext.calculateParameterInfo(leftColumn);
        RenderedParameterInfo parameterInfo2 = renderingContext.calculateParameterInfo(leftColumn);

        String finalFragment = operator1()
                + spaceBefore(parameterInfo1.renderedPlaceHolder())
                + spaceBefore(operator2())
                + spaceBefore(parameterInfo2.renderedPlaceHolder());

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameter(parameterInfo1.parameterMapKey(), leftColumn.convertParameterType(value1()))
                .withParameter(parameterInfo2.parameterMapKey(), leftColumn.convertParameterType(value2()))
                .build();
    }

    /**
     * Conditions may implement Filterable to add optionality to rendering.
     *
     * <p>If a condition is Filterable, then a user may add a filter to the usage of the condition that makes a decision
     * whether to render the condition at runtime. Conditions that fail the filter will be dropped from the
     * rendered SQL.
     *
     * <p>Implementations of Filterable may call
     * {@link AbstractTwoValueCondition#filterSupport(Predicate, Supplier, AbstractTwoValueCondition)}
     * or {@link AbstractTwoValueCondition#filterSupport(BiPredicate, Supplier, AbstractTwoValueCondition)} as
     * a common implementation of the filtering algorithm.
     *
     * @param <T> the Java type related to the database column type
     */
    public interface Filterable<T> {
        /**
         * If renderable and the values match the predicate, returns this condition. Else returns a condition
         *     that will not render.
         *
         * @param predicate predicate applied to the values, if renderable
         * @return this condition if renderable and the values match the predicate, otherwise a condition
         *     that will not render.
         */
        AbstractTwoValueCondition<T> filter(BiPredicate<? super @NonNull T, ? super @NonNull T> predicate);

        /**
         * If renderable and both values match the predicate, returns this condition. Else returns a condition
         *     that will not render. This function implements a short-circuiting test. If the
         *     first value does not match the predicate, then the second value will not be tested.
         *
         * @param predicate predicate applied to both values, if renderable
         * @return this condition if renderable and the values match the predicate, otherwise a condition
         *     that will not render.
         */
        AbstractTwoValueCondition<T> filter(Predicate<? super @NonNull T> predicate);
    }

    /**
     * Conditions may implement Mappable to alter condition values or types during rendering.
     *
     * <p>If a condition is Mappable, then a user may add a mapper to the usage of the condition that can alter the
     * values of a condition, or change that datatype.
     *
     * <p>Implementations of Mappable may call
     * {@link AbstractTwoValueCondition#mapSupport(Function, Function, BiFunction, Supplier)} as
     * a common implementation of the mapping algorithm.
     *
     * @param <T> the Java type related to the database column type
     */
    public interface Mappable<T> {
        /**
         * If renderable, apply the mappings to the values and return a new condition with the new values. Else return a
         * condition that will not render (this).
         *
         * @param mapper1 a mapping function to apply to the first value, if renderable
         * @param mapper2 a mapping function to apply to the second value, if renderable
         * @param <R> type of the new condition
         * @return a new condition with the result of applying the mappers to the values of this condition,
         *     if renderable, otherwise a condition that will not render.
         */
        <R> AbstractTwoValueCondition<R> map(Function<? super @NonNull T, ? extends R> mapper1,
                                             Function<? super @NonNull T, ? extends R> mapper2);

        /**
         * If renderable, apply the mapping to both values and return a new condition with the new values. Else return a
         *     condition that will not render (this).
         *
         * @param mapper a mapping function to apply to both values, if renderable
         * @param <R> type of the new condition
         * @return a new condition with the result of applying the mappers to the values of this condition,
         *     if renderable, otherwise a condition that will not render.
         */
        <R> AbstractTwoValueCondition<R> map(Function<? super @NonNull T, ? extends R> mapper);
    }
}
