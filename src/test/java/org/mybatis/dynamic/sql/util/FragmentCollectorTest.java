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

import java.util.Optional;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.update.render.UpdateFragmentCollector;
import org.mybatis.dynamic.sql.where.render.WhereFragmentCollector;
import org.mybatis.dynamic.sql.where.render.WhereFragmentCollector.Triple;

public class FragmentCollectorTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
    
    @Test
    public void testWhereFragmentCollectorMerge() {
        WhereFragmentCollector fc1 = new WhereFragmentCollector();
        Triple t1 = Triple.of("p1",  ":p1",  1);
        fc1.add(t1);
        
        WhereFragmentCollector fc2 = new WhereFragmentCollector();
        Triple t2 = Triple.of("p2",  ":p2",  2);
        fc2.add(t2);
        
        fc1 = fc1.merge(fc2);
        
        softly.assertThat(fc1.fragments.size()).isEqualTo(2);
        softly.assertThat(fc1.fragments.get(0)).isEqualTo(":p1");
        softly.assertThat(fc1.fragments.get(1)).isEqualTo(":p2");
        
        softly.assertThat(fc1.parameters.size()).isEqualTo(2);
        softly.assertThat(fc1.parameters.get("p1")).isEqualTo(1);
        softly.assertThat(fc1.parameters.get("p2")).isEqualTo(2);
    }

    @Test
    public void testUpdateFragmentCollectorMerge() {
        SqlTable myTable = SqlTable.of("my_table");
        UpdateFragmentCollector fc1 = new UpdateFragmentCollector(myTable, Optional.empty());
        FragmentAndParameters fp1 = new FragmentAndParameters.Builder("fragment1").build();
        fc1.add(fp1);
        
        UpdateFragmentCollector fc2 = new UpdateFragmentCollector(myTable, Optional.empty());
        FragmentAndParameters fp2 = new FragmentAndParameters.Builder("fragment2").build();
        fc2.add(fp2);
        
        fc1 = fc1.merge(fc2);
        
        softly.assertThat(fc1.fragments.size()).isEqualTo(2);
        softly.assertThat(fc1.fragments.get(0)).isEqualTo("fragment1");
        softly.assertThat(fc1.fragments.get(1)).isEqualTo("fragment2");
        
        softly.assertThat(fc1.parameters.size()).isEqualTo(0);
    }
}
