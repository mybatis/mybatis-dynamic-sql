/*
 *    Copyright 2016-2024 the original author or authors.
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

public interface ThenDSL<T> {

    default T then(String value) {
        return then(StringConstant.of(value));
    }

    default T then(Boolean value) {
        return then(Constant.of(value.toString()));
    }

    default T then(Integer value) {
        return then(Constant.of(value.toString()));
    }

    default T then(Long value) {
        return then(Constant.of(value.toString()));
    }

    default T then(Double value) {
        return then(Constant.of(value.toString()));
    }

    T then(BasicColumn column);
}
