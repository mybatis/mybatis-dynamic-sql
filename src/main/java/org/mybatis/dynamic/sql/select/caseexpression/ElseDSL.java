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
package org.mybatis.dynamic.sql.select.caseexpression;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.Constant;
import org.mybatis.dynamic.sql.StringConstant;

public interface ElseDSL<T> {

    @SuppressWarnings("java:S100")
    default T else_(String value) {
        return else_(StringConstant.of(value));
    }

    @SuppressWarnings("java:S100")
    default T else_(Boolean value) {
        return else_(Constant.of(value.toString()));
    }

    @SuppressWarnings("java:S100")
    default T else_(Integer value) {
        return else_(Constant.of(value.toString()));
    }

    @SuppressWarnings("java:S100")
    default T else_(Long value) {
        return else_(Constant.of(value.toString()));
    }

    @SuppressWarnings("java:S100")
    default T else_(Double value) {
        return else_(Constant.of(value.toString()));
    }

    @SuppressWarnings("java:S100")
    T else_(BasicColumn column);
}
