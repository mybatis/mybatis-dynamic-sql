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

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.reflection.ReflectionException;

import examples.simple.SimpleTableRecord;

@RunWith(JUnitPlatform.class)
public class BeanPropertyGetterTest {
    @Test
    public void testSimpleProperty() {
        SimpleTableRecord record = new SimpleTableRecord();
        record.setId(22);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(record, "id")).isEqualTo(22);
    }

    @Test
    public void testIndexOnNonIndexedProperty() {
        SimpleTableRecord record = new SimpleTableRecord();
        record.setId(22);
        
        assertThatThrownBy(() -> {
            BeanPropertyGetter.instance().getPropertyValue(record, "id[3]");
        }).isInstanceOf(ReflectionException.class);
    }
    
    @Test
    public void testSimpleMap() {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put("id", 33);
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "id")).isEqualTo(33);
    }

    @Test
    public void testComplexMap() {
        Map<String, Map<String, Integer>> testMap = new HashMap<>();
        Map<String, Integer> innerMap = new HashMap<>();
        innerMap.put("id", 33);
        testMap.put("innerMap", innerMap);
        
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "innerMap.id")).isEqualTo(33);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "innerMap[id]")).isEqualTo(33);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(testMap, "innerMap"))
                    .isInstanceOf(HashMap.class);
        });
    }

    @Test
    public void testPrimitiveValue() {
        ClassWithPrimitiveProperties testClass = new ClassWithPrimitiveProperties(2, 3);
        
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(testClass, "id")).isEqualTo(2);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(testClass, "privateId")).isEqualTo(3);
        });
    }
    
    @Test
    public void testNull() {
        assertThat(BeanPropertyGetter.instance().getPropertyValue(null, "id")).isNull();
    }

    @Test
    public void testComplexObject() {
        ComplexObject complexObject = new ComplexObject();
        
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "b")).isEqualTo(true);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "dummy")).isNull();
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "id")).isEqualTo(3);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "description")).isEqualTo("Outer Class");
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "names.Fred")).isEqualTo("Flintstone");
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.dummy")).isNull();
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.dummy.dummy")).isNull();
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.id")).isEqualTo(33);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.description")).isEqualTo("Inner Class");
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.names.Barney")).isEqualTo("Rubble");
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.names[Barney]")).isEqualTo("Rubble");
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.ints[0]")).isEqualTo(44);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.ints[1]")).isEqualTo(55);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.active")).isEqualTo(true);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.firstName")).isEqualTo("Bamm Bamm");
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClass.a")).isEqualTo("a");
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassList[0].ints[0]")).isEqualTo(44);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassList[2].ints[1]")).isEqualTo(55);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassMap[first].ints[0]")).isEqualTo(44);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassMap[second].ints[1]")).isEqualTo(55);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClassMap.first.ints[0]")).isEqualTo(44);
            softly.assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "innerClasses[1].id")).isEqualTo(33);
        });
    }
    
    @Test
    public void testThatIsMethodsAreSelectedBeforeGetMethods() {
        ComplexObject complexObject = new ComplexObject();
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "booleanProperty")).isEqualTo(false);
    }

    @Test
    public void testThatInvocationTargetExceptionsReturnNull() {
        ComplexObject complexObject = new ComplexObject();
        
        assertThat(BeanPropertyGetter.instance().getPropertyValue(complexObject, "throwsException")).isNull();
    }
}
