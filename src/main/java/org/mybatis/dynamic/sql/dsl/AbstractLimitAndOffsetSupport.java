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

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.Buildable;

public abstract class AbstractLimitAndOffsetSupport<T extends
        LimitAndOffsetOperations<T, M> & Buildable<M> & ForAndWaitOperations<T>, M>
        implements LimitAndOffsetOperations<T, M> {
    private @Nullable Long limit;
    private @Nullable Long offset;
    private @Nullable Long fetchFirstRows;
    private final T delegate;

    protected AbstractLimitAndOffsetSupport(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public LimitFinisher<T, M> limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return new MyLimitFinisher();
    }

    @Override
    public OffsetFirstFinisher<T, M> offsetWhenPresent(@Nullable Long offset) {
        this.offset = offset;
        return new MyOffsetFirstFinisher();
    }

    @Override
    public FetchFirstFinisher<T> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return new MyFetchFirstFinisher();
    }

    protected abstract T getThis();

    protected Optional<PagingModel> toPagingModel() {
        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }

    public class MyLimitFinisher implements LimitFinisher<T, M> {
        @Override
        public T offsetWhenPresent(@Nullable Long offset) {
            AbstractLimitAndOffsetSupport.this.offset = offset;
            return AbstractLimitAndOffsetSupport.this.getThis();
        }

        @Override
        public T setForClause(String forClause) {
            return AbstractLimitAndOffsetSupport.this.delegate.setForClause(forClause);
        }

        @Override
        public T setWaitClause(String waitClause) {
            return AbstractLimitAndOffsetSupport.this.delegate.setWaitClause(waitClause);
        }

        @Override
        public M build() {
            return delegate.build();
        }
    }

    public class MyOffsetFirstFinisher implements OffsetFirstFinisher<T, M> {
        @Override
        public FetchFirstFinisher<T> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            return AbstractLimitAndOffsetSupport.this.fetchFirstWhenPresent(fetchFirstRows);
        }

        @Override
        public T setForClause(String forClause) {
            return AbstractLimitAndOffsetSupport.this.delegate.setForClause(forClause);
        }

        @Override
        public T setWaitClause(String waitClause) {
            return AbstractLimitAndOffsetSupport.this.delegate.setWaitClause(waitClause);
        }

        @Override
        public M build() {
            return delegate.build();
        }
    }

    public class MyFetchFirstFinisher implements FetchFirstFinisher<T> {
        @Override
        public T rowsOnly() {
            return AbstractLimitAndOffsetSupport.this.getThis();
        }
    }
}
