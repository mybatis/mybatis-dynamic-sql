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
package org.mybatis.dynamic.sql.util;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface Utilities {
    static <T> T buildIfNecessary(@Nullable T current, @NonNull Supplier<T> builder) {
        return current == null ? builder.get() : current;
    }

    static long safelyUnbox(@Nullable Long l) {
        return l == null ? 0 : l;
    }

    static <T> Stream<@NonNull T> filterNullValues(Stream<@Nullable T> values) {
        return values.filter(Objects::nonNull);
    }

    static <T> Collection<@NonNull T> removeNullElements(Collection<@Nullable T> values) {
        return filterNullValues(values.stream()).toList();
    }
}
