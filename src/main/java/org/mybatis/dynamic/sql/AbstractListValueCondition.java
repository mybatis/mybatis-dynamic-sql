/*
 *    Copyright 2016-2020 the original author or authors.
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
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractListValueCondition<T, S extends AbstractListValueCondition<T, S>>
        implements VisitableCondition<T> {
    protected final Collection<T> values;
    protected final UnaryOperator<Stream<T>> valueStreamTransformer;
    protected final Callback emptyCallback;

    protected AbstractListValueCondition(Collection<T> values) {
        this(values, UnaryOperator.identity(), () -> { });
    }

    protected AbstractListValueCondition(Collection<T> values, UnaryOperator<Stream<T>> valueStreamTransformer) {
        this(values, valueStreamTransformer, () -> { });
    }

    protected AbstractListValueCondition(Collection<T> values, UnaryOperator<Stream<T>> valueStreamTransformer,
            Callback emptyCallback) {
        this.valueStreamTransformer = Objects.requireNonNull(valueStreamTransformer);
        this.values = valueStreamTransformer.apply(Objects.requireNonNull(values).stream())
            .collect(Collectors.toList());
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

    public abstract S withListEmptyCallback(Callback callback);

    public abstract String renderCondition(String columnName, Stream<String> placeholders);
}
