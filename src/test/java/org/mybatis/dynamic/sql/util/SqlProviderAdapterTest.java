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
package org.mybatis.dynamic.sql.util;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SqlProviderAdapterTest {
    @Test
    void testThatInsertMultipleWithGeneratedKeysThrowsException() {
        Map<String, Object> parameters = new HashMap<>();
        SqlProviderAdapter adapter = new SqlProviderAdapter();

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> adapter.insertMultipleWithGeneratedKeys(parameters)).withMessage(
                        "The parameters for insertMultipleWithGeneratedKeys must contain exactly one parameter of type String");
    }
}
