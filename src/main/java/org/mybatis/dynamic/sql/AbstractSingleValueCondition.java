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

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public abstract class AbstractSingleValueCondition<T> implements VisitableCondition<T> {
    protected final T value;

    protected AbstractSingleValueCondition(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    protected <S extends AbstractSingleValueCondition<T>> S filterSupport(Predicate<? super T> predicate,
            Supplier<S> emptySupplier, S self) {
        if (isEmpty()) {
            return self;
        } else {
            return predicate.test(value) ? self : emptySupplier.get();
        }
    }

    protected <R, S extends AbstractSingleValueCondition<R>> S mapSupport(Function<? super T, ? extends R> mapper,
            Function<R, S> constructor, Supplier<S> emptySupplier) {
        if (isEmpty()) {
            return emptySupplier.get();
        } else {
            return constructor.apply(mapper.apply(value));
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

    public abstract String operator();

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(leftColumn);
        String finalFragment = operator() + spaceBefore(parameterInfo.renderedPlaceHolder());

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameter(parameterInfo.parameterMapKey(), leftColumn.convertParameterType(value()))
                .build();
    }
}
