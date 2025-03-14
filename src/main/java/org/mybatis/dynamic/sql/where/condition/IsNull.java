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
package org.mybatis.dynamic.sql.where.condition;

import java.util.function.BooleanSupplier;

import org.mybatis.dynamic.sql.AbstractNoValueCondition;

public class IsNull<T> extends AbstractNoValueCondition<T> implements AbstractNoValueCondition.Filterable {
    private static final IsNull<?> EMPTY = new IsNull<>() {
        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsNull<T> empty() {
        @SuppressWarnings("unchecked")
        IsNull<T> t = (IsNull<T>) EMPTY;
        return t;
    }

    public IsNull() {
        super();
    }

    @Override
    public String operator() {
        return "is null"; //$NON-NLS-1$
    }

    @Override
    public <S> IsNull<S> filter(BooleanSupplier booleanSupplier) {
        @SuppressWarnings("unchecked")
        IsNull<S> self = (IsNull<S>) this;
        return filterSupport(booleanSupplier, IsNull::empty, self);
    }
}
