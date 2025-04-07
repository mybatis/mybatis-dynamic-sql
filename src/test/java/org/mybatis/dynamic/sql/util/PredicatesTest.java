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
package org.mybatis.dynamic.sql.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PredicatesTest {
    @Test
    void testFirstNull() {
        assertThat(Predicates.bothPresent().test(null, 1)).isFalse();
    }

    @Test
    void testSecondNull() {
        assertThat(Predicates.bothPresent().test(1, null)).isFalse();
    }

    @Test
    void testBothNull() {
        assertThat(Predicates.bothPresent().test(null, null)).isFalse();
    }

    @Test
    void testNeitherNull() {
        assertThat(Predicates.bothPresent().test(1, 1)).isTrue();
    }
}
