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
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import examples.simple.SimpleTableRecord;

public class BeanPropertyGetterTest {

    @Test
    public void testSimpleProperty() {
        SimpleTableRecord record = new SimpleTableRecord();
        record.setId(22);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(record, "id"), is(22));
    }

    @Test
    public void testSimpleMap() {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put("id", 33);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "id"), is(33));
    }

    @Test
    public void testPrimitiveValue() {
        ClassWithPrimitiveProperties testClass = new ClassWithPrimitiveProperties(2, 3);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testClass, "id"), is(notNullValue()));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testClass, "privateId"), is(notNullValue()));
    }
}
