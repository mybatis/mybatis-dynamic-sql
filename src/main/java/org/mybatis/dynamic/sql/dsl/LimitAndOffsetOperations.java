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

public interface LimitAndOffsetOperations
        <R, T extends LimitAndOffsetOperations<R, T> & Buildable<R> & ForAndWaitOperations<T>> {
    default LimitFinisher<R, T> limit(long limit) {
        return limitWhenPresent(limit);
    }

    LimitFinisher<R, T> limitWhenPresent(@Nullable Long limit);

    default OffsetFirstFinisher<R, T> offset(long offset) {
        return offsetWhenPresent(offset);
    }

    OffsetFirstFinisher<R, T> offsetWhenPresent(@Nullable Long offset);

    default FetchFirstFinisher<T> fetchFirst(long fetchFirstRows) {
        return fetchFirstWhenPresent(fetchFirstRows);
    }

    FetchFirstFinisher<T> fetchFirstWhenPresent(@Nullable Long fetchFirstRows);

    class OffsetFirstFinisher<R, T extends LimitAndOffsetOperations<R, T> & Buildable<R> & ForAndWaitOperations<T>>
            implements Buildable<R>, ForAndWaitOperations<T> {
        private final T delegate;

        public OffsetFirstFinisher(T delegate) {
            this.delegate = delegate;
        }

        public FetchFirstFinisher<T> fetchFirst(long fetchFirstRows) {
            return fetchFirstWhenPresent(fetchFirstRows);
        }

        public FetchFirstFinisher<T> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            delegate.fetchFirstWhenPresent(fetchFirstRows);
            return new FetchFirstFinisher<>(delegate);
        }

        @Override
        public T setWaitClause(String waitClause) {
            return delegate.setWaitClause(waitClause);
        }

        @Override
        public T setForClause(String forClause) {
            return delegate.setForClause(forClause);
        }

        @Override
        public R build() {
            return delegate.build();
        }
    }

    class LimitFinisher<R, T extends LimitAndOffsetOperations<R, T> & Buildable<R> & ForAndWaitOperations<T>>
            implements Buildable<R>, ForAndWaitOperations<T> {
        private final T delegate;

        public LimitFinisher(T delegate) {
            this.delegate = delegate;
        }

        public T offset(long offset) {
            return offsetWhenPresent(offset);
        }

        public T offsetWhenPresent(@Nullable Long offset) {
            delegate.offsetWhenPresent(offset);
            return delegate;
        }

        @Override
        public T setWaitClause(String waitClause) {
            return delegate.setWaitClause(waitClause);
        }

        @Override
        public T setForClause(String forClause) {
            return delegate.setForClause(forClause);
        }

        @Override
        public R build() {
            return delegate.build();
        }
    }

    class FetchFirstFinisher<T> {
        private final T delegate;

        public FetchFirstFinisher(T delegate) {
            this.delegate = delegate;
        }

        public T rowsOnly() {
            return delegate;
        }
    }
}
