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

    /**
     * If not empty, apply the predicate to each value in the list and return a new condition with the filtered values.
     *     Else returns an empty condition (this).
     *
     * @param predicate predicate applied to the values, if not empty
     *
     * @return a new condition with filtered values if renderable, otherwise an empty condition
     */
    public abstract AbstractListValueCondition<T> filter(Predicate<? super T> predicate);

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
}
