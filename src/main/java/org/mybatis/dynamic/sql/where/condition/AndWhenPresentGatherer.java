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

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * Utility class supporting the "and" part of a between when present condition. This class supports builders,
 * so it is mutable.
 *
 * @author Jeff Butler
 *
 * @param <T>
 *            the type of field for the between condition
 * @param <R>
 *            the type of condition being built
 */
public abstract class AndWhenPresentGatherer<T, R> {
    protected final @Nullable T value1;

    protected AndWhenPresentGatherer(@Nullable T value1) {
        this.value1 = value1;
    }

    public R and(@Nullable T value2) {
        return build(value2);
    }

    public R and(Supplier<@Nullable T> valueSupplier2) {
        return and(valueSupplier2.get());
    }

    protected abstract R build(@Nullable T value2);
}
