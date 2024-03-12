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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.VisitableCondition;

public class SimpleCaseDSL<T> {
    private final BindableColumn<T> column;
    private final List<SimpleCaseModel.SimpleWhenCondition<T>> whenConditions = new ArrayList<>();
    private String elseValue;

    private SimpleCaseDSL(BindableColumn<T> column) {
        this.column = Objects.requireNonNull(column);
    }

    @SafeVarargs
    public final WhenFinisher when(VisitableCondition<T> condition,
                                   VisitableCondition<T>... subsequentConditions) {
        return when(condition, Arrays.asList(subsequentConditions));
    }

    public WhenFinisher when(VisitableCondition<T> condition,
                             List<VisitableCondition<T>> subsequentConditions) {
        return new WhenFinisher(condition, subsequentConditions);
    }

    public SimpleCaseDSL<T> elseConstant(String value) {
        elseValue = value;
        return this;
    }

    public SimpleCaseModel<T> end() {
        return new SimpleCaseModel.Builder<T>()
                .withColumn(column)
                .withWhenConditions(whenConditions)
                .withElseValue(elseValue)
                .build();
    }

    public class WhenFinisher {
        private final List<VisitableCondition<T>> conditions = new ArrayList<>();

        private WhenFinisher(VisitableCondition<T> condition, List<VisitableCondition<T>> subsequentConditions) {
            conditions.add(condition);
            conditions.addAll(subsequentConditions);
        }

        public SimpleCaseDSL<T> thenConstant(String value) {
            whenConditions.add(new SimpleCaseModel.SimpleWhenCondition<>(conditions, value));
            return SimpleCaseDSL.this;
        }
    }

    public static <T> SimpleCaseDSL<T> simpleCase(BindableColumn<T> column) {
        return new SimpleCaseDSL<>(column);
    }
}
