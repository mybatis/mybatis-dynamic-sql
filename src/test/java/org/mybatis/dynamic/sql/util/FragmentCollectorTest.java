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
package org.mybatis.dynamic.sql.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class FragmentCollectorTest {

    @Test
    public void testFragmentCollectorMerge() {
        FragmentCollector fc1 = new FragmentCollector();
        FragmentCollector.Triple<Integer> t1 = FragmentCollector.Triple.of("p1",  ":p1",  1);
        fc1.add(t1);
        
        FragmentCollector fc2 = new FragmentCollector();
        FragmentCollector.Triple<Integer> t2 = FragmentCollector.Triple.of("p2",  ":p2",  2);
        fc2.add(t2);
        
        fc1 = fc1.merge(fc2);
        
        assertThat(fc1.fragments.size(), is(2));
        assertThat(fc1.fragments.get(0), is(":p1"));
        assertThat(fc1.fragments.get(1), is(":p2"));
        
        assertThat(fc1.parameters.size(), is(2));
        assertThat(fc1.parameters.get("p1"), is(1));
        assertThat(fc1.parameters.get("p2"), is(2));
    }
}
