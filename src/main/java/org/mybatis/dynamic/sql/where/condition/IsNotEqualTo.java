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

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.AbstractSingleValueCondition;

public class IsNotEqualTo<T> extends AbstractSingleValueCondition<T> {

    protected IsNotEqualTo(Supplier<T> valueSupplier) {
        super(valueSupplier);
    }

    protected IsNotEqualTo(Supplier<T> valueSupplier, Predicate<T> predicate) {
        super(valueSupplier, predicate);
    }

    @Override
    public String renderCondition(String columnName, String placeholder) {
        return columnName + " <> " + placeholder; //$NON-NLS-1$
    }
    
    public static <T> IsNotEqualTo<T> of(Supplier<T> valueSupplier) {
        return new IsNotEqualTo<>(valueSupplier);
    }
    
    public IsNotEqualTo<T> when(Predicate<T> predicate) {
        return new IsNotEqualTo<>(valueSupplier, predicate);
    }

    public IsNotEqualTo<T> then(UnaryOperator<T> transformer) {
        return shouldRender() ? new IsNotEqualTo<>(() -> transformer.apply(value())) : this;
    }
}
