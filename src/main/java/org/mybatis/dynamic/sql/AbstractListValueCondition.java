/**
 *    Copyright 2016-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class AbstractListValueCondition<T> implements VisitableCondition<T> {
    protected List<T> values;
    protected UnaryOperator<Stream<T>> valueStreamOperations;

    protected AbstractListValueCondition(List<T> values) {
        this(values, UnaryOperator.identity());
    }

    protected AbstractListValueCondition(List<T> values, UnaryOperator<Stream<T>> valueStreamOperations) {
        this.values = new ArrayList<>(Objects.requireNonNull(values));
        this.valueStreamOperations = Objects.requireNonNull(valueStreamOperations);
    }
    
    public final <R> Stream<R> mapValues(Function<T, R> mapper) {
        return valueStreamOperations.apply(values.stream()).map(mapper);
    }
    
    @Override
    public <R> R accept(ConditionVisitor<T, R> visitor) {
        return visitor.visit(this);
    }

    public abstract String renderCondition(String columnName, Stream<String> placeholders);
}
