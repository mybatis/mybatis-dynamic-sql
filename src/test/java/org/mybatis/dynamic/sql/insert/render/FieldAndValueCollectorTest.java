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
package org.mybatis.dynamic.sql.insert.render;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FieldAndValueCollectorTest {

    @Test
    void testMerge() {
        FieldAndValueCollector collector1 = new FieldAndValueCollector();
        FieldAndValueAndParameters fvp1 = FieldAndValueAndParameters.withFieldName("f1").withValuePhrase("3").build();
        collector1.add(fvp1);

        FieldAndValueCollector collector2 = new FieldAndValueCollector();
        FieldAndValueAndParameters fvp2 = FieldAndValueAndParameters.withFieldName("f2").withValuePhrase("4").build();
        collector2.add(fvp2);

        collector1.merge(collector2);

        assertThat(collector1.columnsPhrase()).isEqualTo("(f1, f2)");
        assertThat(collector1.valuesPhrase()).isEqualTo("values (3, 4)");
    }
}
