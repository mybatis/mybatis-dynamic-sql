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
package org.mybatis.dynamic.sql;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class SqlTableTest {

    @Test
    public void testEqualsNull() {
        SqlTable table = SqlTable.of("fred");
        assertThat(table.equals(null)).isFalse();
    }

    @Test
    public void testEqualsSame() {
        SqlTable table1 = SqlTable.of("fred");
        SqlTable table2 = SqlTable.of("fred");
        assertThat(table1.equals(table2)).isTrue();
    }

    @Test
    public void testEqualsIdentical() {
        SqlTable table = SqlTable.of("fred");
        assertThat(table.equals(table)).isTrue();
    }
    
    @Test
    public void testEqualsDifferent() {
        SqlTable table1 = SqlTable.of("fred");
        SqlTable table2 = SqlTable.of("betty");
        assertThat(table1.equals(table2)).isFalse();
    }

    @Test
    public void testEqualsDifferentObject() {
        SqlTable table = SqlTable.of("fred");
        assertThat(table.equals(1)).isFalse();
    }
}
