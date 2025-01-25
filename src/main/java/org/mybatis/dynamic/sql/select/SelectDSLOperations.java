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

public interface SelectDSLOperations<R> extends SelectDSLForAndWaitOperations<R> {
    default SelectDSL<R>.LimitFinisher limit(long limit) {
        return getSelectDSL().limit(limit);
    }

    default SelectDSL<R>.LimitFinisher limitWhenPresent(@Nullable Long limit) {
        return getSelectDSL().limitWhenPresent(limit);
    }

    default SelectDSL<R>.OffsetFirstFinisher offset(long offset) {
        return getSelectDSL().offset(offset);
    }

    default SelectDSL<R>.OffsetFirstFinisher offsetWhenPresent(@Nullable Long offset) {
        return getSelectDSL().offsetWhenPresent(offset);
    }

    default SelectDSL<R>.FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        return getSelectDSL().fetchFirst(fetchFirstRows);
    }

    default SelectDSL<R>.FetchFirstFinisher fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        return getSelectDSL().fetchFirstWhenPresent(fetchFirstRows);
    }
}
