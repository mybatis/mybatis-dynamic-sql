/*
 *    Copyright 2016-2021 the original author or authors.
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

import java.util.function.BooleanSupplier;

import org.mybatis.dynamic.sql.AbstractNoValueCondition;

public class IsNull<T> extends AbstractNoValueCondition<T> {

    public IsNull() {
        super();
    }

    @Override
    public String renderCondition(String columnName) {
        return columnName + " is null"; //$NON-NLS-1$
    }

    /**
     * If the supplier returns true, returns this condition. Else returns a condition that will not render.
     *
     * @deprecated replaced by {@link IsNull#filter(BooleanSupplier)}
     * @param booleanSupplier function that specifies whether the condition should render
     * @param <S> condition type - not used except for compilation compliance
     * @return If the condition should render, returns this condition. Else a condition that will not
     *     render.
     */
    @Deprecated
    public <S> IsNull<S> when(BooleanSupplier booleanSupplier) {
        return filter(booleanSupplier);
    }

    /**
     * If the supplier returns true, returns this condition. Else returns a condition that will not render.
     *
     * @param booleanSupplier function that specifies whether the condition should render
     * @param <S> condition type - not used except for compilation compliance
     * @return If the condition should render, returns this condition. Else a condition that will not
     *     render.
     */
    public <S> IsNull<S> filter(BooleanSupplier booleanSupplier) {
        if (booleanSupplier.getAsBoolean()) {
            @SuppressWarnings("unchecked")
            IsNull<S> self = (IsNull<S>) this;
            return self;
        } else {
            return EmptyIsNull.empty();
        }
    }

    public static class EmptyIsNull<T> extends IsNull<T> {
        private static final EmptyIsNull<?> EMPTY = new EmptyIsNull<>();

        public static <T> EmptyIsNull<T> empty() {
            @SuppressWarnings("unchecked")
            EmptyIsNull<T> t = (EmptyIsNull<T>) EMPTY;
            return t;
        }

        private EmptyIsNull() {
            super();
        }

        @Override
        public boolean shouldRender() {
            return false;
        }
    }
}
