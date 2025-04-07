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

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public abstract class AbstractListValueCondition<T> implements RenderableCondition<T> {
    protected final Collection<T> values;

    protected AbstractListValueCondition(Collection<T> values) {
        this.values = Objects.requireNonNull(values);
    }

    public final Stream<T> values() {
        return values.stream();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    private <R> Collection<R> applyMapper(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return values().map(mapper).collect(Collectors.toList());
    }

    private Collection<T> applyFilter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return values().filter(predicate).toList();
    }

    protected <S extends AbstractListValueCondition<T>> S filterSupport(Predicate<? super T> predicate,
            Function<Collection<T>, S> constructor, S self, Supplier<S> emptySupplier) {
        if (isEmpty()) {
            return self;
        } else {
            Collection<T> filtered = applyFilter(predicate);
            return filtered.isEmpty() ? emptySupplier.get() : constructor.apply(filtered);
        }
    }

    protected <R, S extends AbstractListValueCondition<R>> S mapSupport(Function<? super T, ? extends R> mapper,
            Function<Collection<R>, S> constructor, Supplier<S> emptySupplier) {
        if (isEmpty()) {
            return emptySupplier.get();
        } else {
            return constructor.apply(applyMapper(mapper));
        }
    }

    public abstract String operator();

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        return values().map(v -> toFragmentAndParameters(v, renderingContext, leftColumn))
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(",", //$NON-NLS-1$
                        operator() + " (", ")")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private FragmentAndParameters toFragmentAndParameters(T value, RenderingContext renderingContext,
                                                          BindableColumn<T> leftColumn) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(leftColumn);
        return FragmentAndParameters.withFragment(parameterInfo.renderedPlaceHolder())
                .withParameter(parameterInfo.parameterMapKey(), leftColumn.convertParameterType(value))
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
     * {@link AbstractListValueCondition#filterSupport(Predicate, Function, AbstractListValueCondition, Supplier)} as
     * a common implementation of the filtering algorithm.
     *
     * @param <T> the Java type related to the database column type
     */
    public interface Filterable<T> {
        /**
         * If renderable and the value matches the predicate, returns this condition. Else returns a condition
         *     that will not render.
         *
         * @param predicate predicate applied to the value, if renderable
         * @return this condition if renderable and the value matches the predicate, otherwise a condition
         *     that will not render.
         */
        AbstractListValueCondition<T> filter(Predicate<? super @NonNull T> predicate);
    }

    /**
     * Conditions may implement Mappable to alter condition values or types during rendering.
     *
     * <p>If a condition is Mappable, then a user may add a mapper to the usage of the condition that can alter the
     * values of a condition, or change that datatype.
     *
     * <p>Implementations of Mappable may call
     * {@link AbstractListValueCondition#mapSupport(Function, Function, Supplier)} as
     * a common implementation of the mapping algorithm.
     *
     * @param <T> the Java type related to the database column type
     */
    public interface Mappable<T> {
        /**
         * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
         * condition that will not render (this).
         *
         * @param mapper a mapping function to apply to the value, if renderable
         * @param <R> type of the new condition
         * @return a new condition with the result of applying the mapper to the value of this condition,
         *     if renderable, otherwise a condition that will not render.
         */
        <R> AbstractListValueCondition<R> map(Function<? super @NonNull T, ? extends R> mapper);
    }
}
