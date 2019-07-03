/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.where.condition;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.util.Predicates;

public class IsNotBetweenWhenPresent<T> extends IsNotBetween<T> {

    protected IsNotBetweenWhenPresent(Supplier<T> valueSupplier1, Supplier<T> valueSupplier2) {
        super(valueSupplier1, valueSupplier2, Predicates.bothPresent());
    }
    
    public static class Builder<T> extends AndGatherer<T, IsNotBetweenWhenPresent<T>> {
        private Builder(Supplier<T> valueSupplier1) {
            super(valueSupplier1);
        }
        
        @Override
        protected IsNotBetweenWhenPresent<T> build() {
            return new IsNotBetweenWhenPresent<>(valueSupplier1, valueSupplier2);
        }
    }
    
    public static <T> Builder<T> isNotBetweenWhenPresent(Supplier<T> valueSupplier) {
        return new Builder<>(valueSupplier);
    }

    @Override
    public IsNotBetweenWhenPresent<T> then(UnaryOperator<T> transformer1, UnaryOperator<T> transformer2) {
        return shouldRender() ? new IsNotBetweenWhenPresent<>(() -> transformer1.apply(value1()),
                () -> transformer2.apply(value2())) : this;
    }
}
