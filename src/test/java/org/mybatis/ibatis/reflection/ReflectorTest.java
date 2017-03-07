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
package org.mybatis.ibatis.reflection;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ReflectorTest {

    @Test
    public void testMethodSignatureWithGetter() throws Exception {
        Reflector reflector = new Reflector(TestClass.class);
        
        String signature = reflector.getMethodSignature(TestClass.class.getMethod("getName"));
        assertThat(signature, is("java.lang.String#getName"));
    }
    
    @Test
    public void testMethodSignatureWithSetter() throws Exception {
        Reflector reflector = new Reflector(TestClass.class);
        
        String signature = reflector.getMethodSignature(TestClass.class.getMethod("setName", String.class));
        assertThat(signature, is("void#setName:java.lang.String"));
    }
    
    @Test
    public void testComplexMethodReturningNull() throws Exception {
        Reflector reflector = new Reflector(TestClass.class);
        
        String signature = reflector.getMethodSignature(TestClass.class.getMethod("setFullName", String.class, String.class));
        assertThat(signature, is("void#setFullName:java.lang.String,java.lang.String"));
    }
    
    @Test
    public void testComplexMethodReturningString() throws Exception {
        Reflector reflector = new Reflector(TestClass.class);
        
        String signature = reflector.getMethodSignature(TestClass.class.getMethod("composeName", String.class, String.class));
        assertThat(signature, is("java.lang.String#composeName:java.lang.String,java.lang.String"));
    }
    
    @Test
    public void testThatMethodsFromSubclassesPrevail() {
        TestClass tc = new TestClass();
        
        Reflector reflector = new Reflector(tc.getClass());
        Map<String, Method> uniqueMethods = new HashMap<>();
        
        reflector.addUniqueMethods(uniqueMethods, TestClass.class.getDeclaredMethods());
        Method toStringMethod = uniqueMethods.get("java.lang.String#toString");
        assertThat(toStringMethod.getDeclaringClass().getName(), is(TestClass.class.getName()));

        reflector.addUniqueMethods(uniqueMethods, Object.class.getDeclaredMethods());
        toStringMethod = uniqueMethods.get("java.lang.String#toString");
        // this tests that the toString method in Object does not replace the toString method from TestClass
        assertThat(toStringMethod.getDeclaringClass().getName(), is(TestClass.class.getName()));
    }

    @Test
    public void testPropertyCaseFixerSingleUpperCaseLetter() {
        assertThat(Reflector.fixPropertyCase("A"), is("a"));
    }
    
    @Test
    public void testPropertyCaseFixerSingleLowerCaseLetter() {
        assertThat(Reflector.fixPropertyCase("a"), is("a"));
    }
    
    @Test
    public void testPropertyCaseFixerCorrectName() {
        assertThat(Reflector.fixPropertyCase("firstName"), is("firstName"));
    }
    
    @Test
    public void testPropertyCaseFixerUpperCaseName() {
        assertThat(Reflector.fixPropertyCase("FirstName"), is("firstName"));
    }
    
    @Test
    public void testPropertyCaseFixerAcronym() {
        assertThat(Reflector.fixPropertyCase("IBM"), is("IBM"));
    }
    
    @Test
    public void testPropertyCaseFixerEmptyProperty() {
        assertThat(Reflector.fixPropertyCase(""), is(""));
    }
    
    @Test
    public void testValidPropertyNameEmpty() {
        assertThat(Reflector.isValidPropertyName(""), is(false));
    }
    
    @Test
    public void testValidPropertyNameClass() {
        assertThat(Reflector.isValidPropertyName("class"), is(false));
    }
    
    @Test
    public void testValidPropertyNameSerialVersionUID() {
        assertThat(Reflector.isValidPropertyName("serialVersionUID"), is(false));
    }
    
    @Test
    public void testValidPropertyNameDollar() {
        assertThat(Reflector.isValidPropertyName("$fred"), is(false));
    }
    
    @Test
    public void testValidPropertyName() {
        assertThat(Reflector.isValidPropertyName("firstName"), is(true));
    }
    
    @Test
    public void someTestSetAccessible() {
        Reflector r = new Reflector(TestClass.class);
        assertThat(r.getGetInvoker("firstName").isPresent(), is(true));
        assertThat(r.getGetInvoker("privateField").isPresent(), is(true));
    }
    
    @Test
    public void someTestDenySetAccessible() {
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(new DenyingSecurityManager());
        Reflector r = new Reflector(TestClass.class);
        System.setSecurityManager(oldSm);
        assertThat(r.getGetInvoker("firstName").isPresent(), is(false));
        assertThat(r.getGetInvoker("privateField").isPresent(), is(false));
    }
    
    public static class TestClass {
        @SuppressWarnings("unused")
        private int privateField;
        public String getName() {
            return "fred";
        }
        
        @SuppressWarnings("unused")
        private String getFirstName() {
            return null;
        }
        
        public void setName(String name) {
            
        }

        public void setFullName(String firstName, String lastName) {
            
        }

        public String composeName(String firstName, String lastName) {
            return null;
        }
        
        @Override
        public String toString() {
            return "This is the TestClass";
        }
    }
}
