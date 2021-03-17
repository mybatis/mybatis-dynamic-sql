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

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractListValueCondition<T, S extends AbstractListValueCondition<T, S>>
        implements VisitableCondition<T> {
    protected final Collection<T> values;
    protected final Callback emptyCallback;

    protected AbstractListValueCondition(Collection<T> values) {
        this(values, () -> { });
    }

    protected AbstractListValueCondition(Collection<T> values, Callback emptyCallback) {
        this.values = Objects.requireNonNull(values);
        this.emptyCallback = Objects.requireNonNull(emptyCallback);
    }

    public final <R> Stream<R> mapValues(Function<T, R> mapper) {
        return values.stream().map(mapper);
    }

    @Override
    public boolean shouldRender() {
        if (values.isEmpty()) {
            emptyCallback.call();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public <R> R accept(ConditionVisitor<T, R> visitor) {
        return visitor.visit(this);
    }

    private Collection<T> applyMapper(UnaryOperator<T> mapper) {
        Objects.requireNonNull(mapper);
        return values.stream().map(mapper).collect(Collectors.toList());
    }

    private Collection<T> applyFilter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        return values.stream().filter(predicate).collect(Collectors.toList());
    }

    protected S filter(Predicate<T> predicate, BiFunction<Collection<T>, Callback, S> constructor, S self) {
        if (shouldRender()) {
            return constructor.apply(applyFilter(predicate), emptyCallback);
        } else {
            return self;
        }
    }

    /**
     * If renderable, apply the predicate to each value in the list and return a new condition with the filtered values.
     *     Else returns a condition that will not render (this). If all values are filtered out of the value
     *     list, then the condition will not render.
     *
     * @param predicate predicate applied to the values, if renderable
     * @return a new condition with filtered values if renderable, otherwise a condition
     *     that will not render.
     */
    public abstract S filter(Predicate<T> predicate);

    protected S map(UnaryOperator<T> mapper, BiFunction<Collection<T>, Callback, S> constructor, S self) {
        if (shouldRender()) {
            return constructor.apply(applyMapper(mapper), emptyCallback);
        } else {
            return self;
        }
    }

    /**
     * If renderable, apply the mapping to each value in the list return a new condition with the mapped values.
     *     Else return a condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the values, if renderable
     * @return a new condition with mapped values if renderable, otherwise a condition
     *     that will not render.
     */
    public abstract S map(UnaryOperator<T> mapper);

    public abstract S withListEmptyCallback(Callback callback);

    public abstract String renderCondition(String columnName, Stream<String> placeholders);
}
