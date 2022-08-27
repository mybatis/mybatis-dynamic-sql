/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.util.function.Function;

/**
 * Provides a callback function for empty "in" conditions.
 *
 * @deprecated in favor of the new statement configuration functionality
 */
@Deprecated
@FunctionalInterface
public interface Callback {
    void call();

    static Callback exceptionThrowingCallback(String message) {
        return exceptionThrowingCallback(message, RuntimeException::new);
    }

    static Callback exceptionThrowingCallback(String message,
            Function<String, ? extends RuntimeException> exceptionBuilder) {
        return () -> {
            throw exceptionBuilder.apply(message);
        };
    }
}
