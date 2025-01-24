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
package org.mybatis.dynamic.sql.select;

import org.jspecify.annotations.Nullable;

public interface SelectDSLOperations<R> {
    default PagingDSL.LimitFinisher<R> limit(long limit) {
        return getSelectDSL().limit(limit);
    }

    default PagingDSL.LimitFinisher<R> limitWhenPresent(@Nullable Long limit) {
        return getSelectDSL().limitWhenPresent(limit);
    }

    default PagingDSL.OffsetFirstFinisher<R> offset(long offset) {
        return getSelectDSL().offset(offset);
    }

    default PagingDSL.OffsetFirstFinisher<R> offsetWhenPresent(@Nullable Long offset) {
        return getSelectDSL().offsetWhenPresent(offset);
    }

    default PagingDSL.FetchFirstFinisher<R> fetchFirst(long fetchFirstRows) {
        return getSelectDSL().fetchFirst(fetchFirstRows);
    }

    default PagingDSL.FetchFirstFinisher<R> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        return getSelectDSL().fetchFirstWhenPresent(fetchFirstRows);
    }

    default SelectDSL<R> forUpdate() {
        return getSelectDSL().forUpdate();
    }

    default SelectDSL<R> forNoKeyUpdate() {
        return getSelectDSL().forNoKeyUpdate();
    }

    default SelectDSL<R> forShare() {
        return getSelectDSL().forShare();
    }

    default SelectDSL<R> forKeyShare() {
        return getSelectDSL().forKeyShare();
    }

    default SelectDSL<R> skipLocked() {
        return getSelectDSL().skipLocked();
    }

    default SelectDSL<R> nowait() {
        return getSelectDSL().nowait();
    }

    /**
     * Gain access to the SelectDSL instance.
     *
     * <p>This is a leak of an implementation detail into the public API. The tradeoff is that it
     * significantly reduces copy/paste code of SelectDSL methods into all the different inner classes of
     * QueryExpressionDSL where they would be needed.
     *
     * @return the SelectDSL instance associated with this interface instance
     */
    SelectDSL<R> getSelectDSL();
}
