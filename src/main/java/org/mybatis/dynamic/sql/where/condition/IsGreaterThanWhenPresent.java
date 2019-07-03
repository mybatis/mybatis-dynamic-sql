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

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class IsGreaterThanWhenPresent<T> extends IsGreaterThan<T> {

    protected IsGreaterThanWhenPresent(Supplier<T> valueSupplier) {
        super(valueSupplier, Objects::nonNull);
    }
    
    public static <T> IsGreaterThanWhenPresent<T> of(Supplier<T> valueSupplier) {
        return new IsGreaterThanWhenPresent<>(valueSupplier);
    }

    @Override
    public IsGreaterThanWhenPresent<T> then(UnaryOperator<T> transformer) {
        return shouldRender() ? new IsGreaterThanWhenPresent<>(() -> transformer.apply(value())) : this;
    }
}
