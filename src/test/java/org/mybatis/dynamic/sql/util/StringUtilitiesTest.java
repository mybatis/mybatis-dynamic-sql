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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilitiesTest {

    @Test
    void testInitialUnderscore() {
        String input = "_USER";
        assertThat(StringUtilities.toCamelCase(input)).isEqualTo("user");
    }

    @Test
    void testSpace() {
        String input = "USER NAME";
        assertThat(StringUtilities.toCamelCase(input)).isEqualTo("userName");
    }

    @Test
    void testNumeric() {
        String input = "USER%NAME%3";
        assertThat(StringUtilities.toCamelCase(input)).isEqualTo("userName3");
    }
}
