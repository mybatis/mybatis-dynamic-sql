/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConditionRendererTest {

    @Test
    public void testTripleCollectorMerge() {
        ConditionRenderer.TripleCollector tc1 = new ConditionRenderer.TripleCollector();
        ConditionRenderer.Triple<Integer> t1 = new ConditionRenderer.Triple<>();
        t1.mapKey = "p1";
        t1.jdbcPlaceholder = ":p1";
        t1.value = 1;
        tc1.add(t1);
        
        ConditionRenderer.TripleCollector tc2 = new ConditionRenderer.TripleCollector();
        ConditionRenderer.Triple<Integer> t2 = new ConditionRenderer.Triple<>();
        t2.mapKey = "p2";
        t2.jdbcPlaceholder = ":p2";
        t2.value = 2;
        tc2.add(t2);
        
        tc1 = tc1.merge(tc2);
        
        assertThat(tc1.jdbcPlaceholders.size(), is(2));
        assertThat(tc1.jdbcPlaceholders.get(0), is(":p1"));
        assertThat(tc1.jdbcPlaceholders.get(1), is(":p2"));
        
        assertThat(tc1.parameters.size(), is(2));
        assertThat(tc1.parameters.get("p1"), is(1));
        assertThat(tc1.parameters.get("p2"), is(2));
    }
}
