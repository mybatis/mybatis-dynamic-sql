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
package org.mybatis.dynamic.sql.select.caseexpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.RenderableCondition;

public class SimpleCaseDSL<T> implements ElseDSL<SimpleCaseDSL<T>.SimpleCaseEnder> {
    private final BindableColumn<T> column;
    private final List<SimpleCaseWhenCondition<T>> whenConditions = new ArrayList<>();
    private @Nullable BasicColumn elseValue;

    private SimpleCaseDSL(BindableColumn<T> column) {
        this.column = Objects.requireNonNull(column);
    }

    @SafeVarargs
    public final ConditionBasedWhenFinisher when(RenderableCondition<T> condition,
                                                 RenderableCondition<T>... subsequentConditions) {
        return when(condition, Arrays.asList(subsequentConditions));
    }

    public ConditionBasedWhenFinisher when(RenderableCondition<T> condition,
                                           List<RenderableCondition<T>> subsequentConditions) {
        return new ConditionBasedWhenFinisher(condition, subsequentConditions);
    }

    @SafeVarargs
    public final BasicWhenFinisher when(T condition, T... subsequentConditions) {
        return when(condition, Arrays.asList(subsequentConditions));
    }

    public BasicWhenFinisher when(T condition, List<T> subsequentConditions) {
        return new BasicWhenFinisher(condition, subsequentConditions);
    }

    @SuppressWarnings("java:S100")
    @Override
    public SimpleCaseEnder else_(BasicColumn column) {
        elseValue = column;
        return new SimpleCaseEnder();
    }

    public SimpleCaseModel<T> end() {
        return new SimpleCaseModel.Builder<T>()
                .withColumn(column)
                .withWhenConditions(whenConditions)
                .withElseValue(elseValue)
                .build();
    }

    public class ConditionBasedWhenFinisher implements ThenDSL<SimpleCaseDSL<T>> {
        private final List<RenderableCondition<T>> conditions = new ArrayList<>();

        private ConditionBasedWhenFinisher(RenderableCondition<T> condition,
                                           List<RenderableCondition<T>> subsequentConditions) {
            conditions.add(condition);
            conditions.addAll(subsequentConditions);
        }

        @Override
        public SimpleCaseDSL<T> then(BasicColumn column) {
            whenConditions.add(new ConditionBasedWhenCondition<>(conditions, column));
            return SimpleCaseDSL.this;
        }
    }

    public class BasicWhenFinisher implements ThenDSL<SimpleCaseDSL<T>> {
        private final List<T> values = new ArrayList<>();

        private BasicWhenFinisher(T value, List<T> subsequentValues) {
            values.add(value);
            values.addAll(subsequentValues);
        }

        @Override
        public SimpleCaseDSL<T> then(BasicColumn column) {
            whenConditions.add(new BasicWhenCondition<>(values, column));
            return SimpleCaseDSL.this;
        }
    }

    public class SimpleCaseEnder {
        public SimpleCaseModel<T> end() {
            return SimpleCaseDSL.this.end();
        }
    }

    public static <T> SimpleCaseDSL<T> simpleCase(BindableColumn<T> column) {
        return new SimpleCaseDSL<>(column);
    }
}
