/*
 *    Copyright 2016-2024 the original author or authors.
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

import static org.mybatis.dynamic.sql.util.StringUtilities.quoteStringForSQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.VisitableCondition;

public class SimpleCaseDSL<T> {
    private final BindableColumn<T> column;
    private final List<SimpleCaseWhenCondition<T>> whenConditions = new ArrayList<>();
    private Object elseValue;

    private SimpleCaseDSL(BindableColumn<T> column) {
        this.column = Objects.requireNonNull(column);
    }

    @SafeVarargs
    public final ConditionBasedWhenFinisher when(VisitableCondition<T> condition,
                                                 VisitableCondition<T>... subsequentConditions) {
        return when(condition, Arrays.asList(subsequentConditions));
    }

    public ConditionBasedWhenFinisher when(VisitableCondition<T> condition,
                                           List<VisitableCondition<T>> subsequentConditions) {
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
    public SimpleCaseEnder else_(String value) {
        elseValue = quoteStringForSQL(value);
        return new SimpleCaseEnder();
    }

    @SuppressWarnings("java:S100")
    public SimpleCaseEnder else_(Object value) {
        elseValue = value;
        return new SimpleCaseEnder();
    }

    public BasicColumn end() {
        return new SimpleCaseModel.Builder<T>()
                .withColumn(column)
                .withWhenConditions(whenConditions)
                .withElseValue(elseValue)
                .build();
    }

    public class ConditionBasedWhenFinisher {
        private final List<VisitableCondition<T>> conditions = new ArrayList<>();

        private ConditionBasedWhenFinisher(VisitableCondition<T> condition, List<VisitableCondition<T>> subsequentConditions) {
            conditions.add(condition);
            conditions.addAll(subsequentConditions);
        }

        public SimpleCaseDSL<T> then(String value) {
            whenConditions.add(new ConditionBasedWhenCondition<>(conditions, quoteStringForSQL(value)));
            return SimpleCaseDSL.this;
        }

        public SimpleCaseDSL<T> then(Object value) {
            whenConditions.add(new ConditionBasedWhenCondition<>(conditions, value));
            return SimpleCaseDSL.this;
        }
    }

    public class BasicWhenFinisher {
        private final List<T> values = new ArrayList<>();

        private BasicWhenFinisher(T value, List<T> subsequentValues) {
            values.add(value);
            values.addAll(subsequentValues);
        }

        public SimpleCaseDSL<T> then(String value) {
            whenConditions.add(new BasicWhenCondition<>(values, quoteStringForSQL(value)));
            return SimpleCaseDSL.this;
        }

        public SimpleCaseDSL<T> then(Object value) {
            whenConditions.add(new BasicWhenCondition<>(values, value));
            return SimpleCaseDSL.this;
        }
    }

    public class SimpleCaseEnder {
        public BasicColumn end() {
            return SimpleCaseDSL.this.end();
        }
    }

    public static <T> SimpleCaseDSL<T> simpleCase(BindableColumn<T> column) {
        return new SimpleCaseDSL<>(column);
    }
}
