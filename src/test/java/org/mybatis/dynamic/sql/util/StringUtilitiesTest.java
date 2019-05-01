/**
 *    Copyright 2016-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class StringUtilitiesTest {

    @Test
    public void testThatUpperCaseIsAppliedAfter() {
        Stream<String> ss = Stream.of("fred", "wilma", "barney", "betty");
        
        UnaryOperator<Stream<String>> valueModifier = s -> s.filter(st -> st.equals("fred"));
        
        UnaryOperator<Stream<String>> ua = StringUtilities.upperCaseAfter(valueModifier);
        
        List<String> list = ua.apply(ss).collect(Collectors.toList());

        assertAll(
                () -> assertThat(list.size()).isEqualTo(1),
                () -> assertThat(list.get(0)).isEqualTo("FRED")
        );
    }
}
