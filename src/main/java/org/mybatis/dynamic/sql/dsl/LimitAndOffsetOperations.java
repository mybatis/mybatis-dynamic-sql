/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.util.Buildable;

public interface LimitAndOffsetOperations<T, M> {
    default LimitFinisher<T, M> limit(long limit) {
        return limitWhenPresent(limit);
    }

    LimitFinisher<T, M> limitWhenPresent(@Nullable Long limit);

    default OffsetFirstFinisher<T, M> offset(long offset) {
        return offsetWhenPresent(offset);
    }

    OffsetFirstFinisher<T, M> offsetWhenPresent(@Nullable Long offset);

    default FetchFirstFinisher<T> fetchFirst(long fetchFirstRows) {
        return fetchFirstWhenPresent(fetchFirstRows);
    }

    FetchFirstFinisher<T> fetchFirstWhenPresent(@Nullable Long fetchFirstRows);

    interface OffsetFirstFinisher<T, M> extends ForAndWaitOperations<T>, Buildable<M> {
        default FetchFirstFinisher<T> fetchFirst(long fetchFirstRows) {
            return fetchFirstWhenPresent(fetchFirstRows);
        }

        FetchFirstFinisher<T> fetchFirstWhenPresent(@Nullable Long fetchFirstRows);
    }

    interface LimitFinisher<T, M> extends ForAndWaitOperations<T>, Buildable<M> {
        default T offset(long offset) {
            return offsetWhenPresent(offset);
        }

        T offsetWhenPresent(@Nullable Long offset);
    }

    interface FetchFirstFinisher<T> {
        T rowsOnly();
    }
}
