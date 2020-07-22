/**
 *    Copyright 2016-2020 the original author or authors.
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
package org.mybatis.dynamic.sql.update.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

class OptionalFragmentCollectorTest {

    @Test
    void testWhereFragmentCollectorMerge() {
        OptionalFragmentCollector fc1 = new OptionalFragmentCollector();
        FragmentAndParameters fp1 = FragmentAndParameters.withFragment(":p1")
                .withParameter("p1", 1)
                .build();
        fc1.add(Optional.of(fp1));

        OptionalFragmentCollector fc2 = new OptionalFragmentCollector();
        fc2.add(Optional.empty());
        FragmentAndParameters fp2 = FragmentAndParameters.withFragment(":p2")
                .withParameter("p2", 2)
                .build();
        fc2.add(Optional.of(fp2));

        fc1.merge(fc2);
        
        assertThat(fc1.fragments().collect(Collectors.toList())).containsExactly(":p1", ":p2");
        assertThat(fc1.parameters()).containsExactly(entry("p1", 1), entry("p2", 2));
    }
}
