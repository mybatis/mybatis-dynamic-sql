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

public class IsNotNull<T> extends AbstractNoValueCondition<T> {
    private static final IsNotNull<?> EMPTY = new IsNotNull<>() {
        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsNotNull<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotNull<T> t = (IsNotNull<T>) EMPTY;
        return t;
    }

    public IsNotNull() {
        super();
    }

    @Override
    public String operator() {
        return "is not null"; //$NON-NLS-1$
    }

    @Override
    public <S> IsNotNull<S> filter(BooleanSupplier booleanSupplier) {
        @SuppressWarnings("unchecked")
        IsNotNull<S> self = (IsNotNull<S>) this;
        return filterSupport(booleanSupplier, IsNotNull::empty, self);
    }
}
