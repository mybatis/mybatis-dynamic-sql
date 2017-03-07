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

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mybatis.ibatis.reflection.ReflectionException;

import examples.simple.SimpleTableRecord;

public class BeanPropertyGetterTest {

    @Test
    public void testSimpleProperty() {
        SimpleTableRecord record = new SimpleTableRecord();
        record.setId(22);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(record, "id"), is(22));
    }

    @Test(expected=ReflectionException.class)
    public void testIndexOnNonIndexedProperty() {
        SimpleTableRecord record = new SimpleTableRecord();
        record.setId(22);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(record, "id[3]"), is(22));
    }
    
    @Test
    public void testSimpleMap() {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put("id", 33);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "id"), is(33));
    }

    @Test
    public void testComplexMap() {
        Map<String, Map<String, Integer>> testMap = new HashMap<>();
        Map<String, Integer> innerMap = new HashMap<>();
        innerMap.put("id", 33);
        testMap.put("innerMap", innerMap);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "innerMap.id"), is(33));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "innerMap[id]"), is(33));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "innerMap").getClass().getName(), is(HashMap.class.getName()));
    }

    @Test
    public void testPrimitiveValue() {
        ClassWithPrimitiveProperties testClass = new ClassWithPrimitiveProperties(2, 3);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testClass, "id"), is(2));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testClass, "privateId"), is(3));
    }
    
    @Test
    public void testNull() {
        assertThat(BeanPropertyGetter.instance().getPropertyValue(null, "id"), is(nullValue()));
    }

    @Test
    public void testComplexObject() {
        ComplexObject complexObject = new ComplexObject();
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "b"), is(true));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "dummy"), is(nullValue()));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "id"), is(3));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "description"), is("Outer Class"));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "names.Fred"), is("Flintstone"));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.dummy"), is(nullValue()));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.dummy.dummy"), is(nullValue()));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.id"), is(33));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.description"), is("Inner Class"));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.names.Barney"), is("Rubble"));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.names[Barney]"), is("Rubble"));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.ints[0]"), is(44));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.ints[1]"), is(55));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.active"), is(true));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.firstName"), is("Bamm Bamm"));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.a"), is("a"));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassList[0].ints[0]"), is(44));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassList[2].ints[1]"), is(55));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassMap[first].ints[0]"), is(44));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassMap[second].ints[1]"), is(55));
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassMap.first.ints[0]"), is(44));

        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClasses[1].id"), is(33));
    }
    
    @Test
    public void testThatIsMethodsAreSelectedBeforeGetMethods() {
        ComplexObject complexObject = new ComplexObject();
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "booleanProperty"), is(false));
    }

    @Test
    public void testThatInvocationTargetExceptionsReturnNull() {
        ComplexObject complexObject = new ComplexObject();
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "throwsException"), is(nullValue()));
    }
}
