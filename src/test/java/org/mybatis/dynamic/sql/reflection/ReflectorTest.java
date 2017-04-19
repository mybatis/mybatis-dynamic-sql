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
package org.mybatis.dynamic.sql.reflection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.mybatis.dynamic.sql.reflection.invoker.Invoker;

public class ReflectorTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testMethodSignatureWithGetter() throws Exception {
        String signature = Reflector.getMethodSignature(TestClass.class.getMethod("getName"));
        assertThat(signature).isEqualTo("java.lang.String#getName");
    }
    
    @Test
    public void testMethodSignatureWithSetter() throws Exception {
        String signature = Reflector.getMethodSignature(TestClass.class.getMethod("setName", String.class));
        assertThat(signature).isEqualTo("void#setName:java.lang.String");
    }
    
    @Test
    public void testComplexMethodReturningNull() throws Exception {
        String signature = Reflector.getMethodSignature(TestClass.class.getMethod("setFullName", String.class, String.class));
        assertThat(signature).isEqualTo("void#setFullName:java.lang.String,java.lang.String");
    }
    
    @Test
    public void testComplexMethodReturningString() throws Exception {
        String signature = Reflector.getMethodSignature(TestClass.class.getMethod("composeName", String.class, String.class));
        assertThat(signature).isEqualTo("java.lang.String#composeName:java.lang.String,java.lang.String");
    }
    
    @Test
    public void testThatMethodsFromSubclassesPrevail() throws Exception {
        TestClass tc = new TestClass();
        Reflector reflector = new Reflector(tc.getClass());
        Optional<Invoker> inv = reflector.getGetInvoker("name");
        Invoker invoker = inv.get();
        Object answer = invoker.invoke(tc, new Object[0]);
        softly.assertThat(answer).isEqualTo("fred");
        softly.assertThat(invoker.getDeclaringClass()).isEqualTo(TestClass.class);
    }

    @Test
    public void testThatFieldsFromSubclassesPrevail() throws Exception {
        TestClass tc = new TestClass();
        Reflector reflector = new Reflector(tc.getClass());
        Optional<Invoker> inv = reflector.getGetInvoker("privateField");
        Invoker invoker = inv.get();
        Object answer = invoker.invoke(tc, new Object[0]);
        softly.assertThat(answer).isEqualTo("Test");
        softly.assertThat(invoker.getDeclaringClass()).isEqualTo(TestClass.class);
    }
    
    @Test
    public void testPropertyCaseFixerSingleUpperCaseLetter() {
        assertThat(Reflector.fixPropertyCase("A")).isEqualTo("a");
    }
    
    @Test
    public void testPropertyCaseFixerSingleLowerCaseLetter() {
        assertThat(Reflector.fixPropertyCase("a")).isEqualTo("a");
    }
    
    @Test
    public void testPropertyCaseFixerCorrectName() {
        assertThat(Reflector.fixPropertyCase("firstName")).isEqualTo("firstName");
    }
    
    @Test
    public void testPropertyCaseFixerUpperCaseName() {
        assertThat(Reflector.fixPropertyCase("FirstName")).isEqualTo("firstName");
    }
    
    @Test
    public void testPropertyCaseFixerAcronym() {
        assertThat(Reflector.fixPropertyCase("IBM")).isEqualTo("IBM");
    }
    
    @Test
    public void testPropertyCaseFixerEmptyProperty() {
        assertThat(Reflector.fixPropertyCase("")).isEqualTo("");
    }
    
    @Test
    public void testValidPropertyNameEmpty() {
        assertThat(Reflector.isValidPropertyName("")).isEqualTo(false);
    }
    
    @Test
    public void testValidPropertyNameClass() {
        assertThat(Reflector.isValidPropertyName("class")).isEqualTo(false);
    }
    
    @Test
    public void testValidPropertyNameSerialVersionUID() {
        assertThat(Reflector.isValidPropertyName("serialVersionUID")).isEqualTo(false);
    }
    
    @Test
    public void testValidPropertyNameDollar() {
        assertThat(Reflector.isValidPropertyName("$fred")).isEqualTo(false);
    }
    
    @Test
    public void testValidPropertyName() {
        assertThat(Reflector.isValidPropertyName("firstName")).isEqualTo(true);
    }
    
    @Test
    public void someTestSetAccessible() {
        Reflector r = new Reflector(TestClass.class);
        softly.assertThat(r.getGetInvoker("firstName").isPresent()).isEqualTo(true);
        softly.assertThat(r.getGetInvoker("privateField").isPresent()).isEqualTo(true);
        softly.assertThat(r.getGetInvoker("name").isPresent()).isEqualTo(true);
        softly.assertThat(r.getGetInvoker("publicField").isPresent()).isEqualTo(true);
        softly.assertThat(r.getGetInvoker("ignored").isPresent()).isEqualTo(false);
    }
    
    @Test
    public void someTestDenySetAccessible() {
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(new DenyingSecurityManager());
        Reflector r = new Reflector(TestClass.class);
        System.setSecurityManager(oldSm);
        softly.assertThat(r.getGetInvoker("firstName").isPresent()).isEqualTo(false);
        softly.assertThat(r.getGetInvoker("privateField").isPresent()).isEqualTo(false);
        // public methods and fields should still be available
        softly.assertThat(r.getGetInvoker("publicField").isPresent()).isEqualTo(true);
        softly.assertThat(r.getGetInvoker("name").isPresent()).isEqualTo(true);
    }
    
    public static class BaseClass<T> {
        @SuppressWarnings("unused")
        private String privateField = "Base";
        
        public String getName() {
            return "wilma";
        }
        
        public T getT() {
            return null;
        }
    }
    
    public static class TestClass extends BaseClass<String> {
        @SuppressWarnings("unused")
        private String privateField = "Test";
        
        public String publicField;

        @Override
        public String getName() {
            return "fred";
        }
        
        @SuppressWarnings("unused")
        private String getFirstName() {
            return null;
        }
        
        // this should cause a bridge method to be generated
        @Override
        public String getT() {
            return null;
        }
        
        public void setName(String name) {
            
        }

        public void setFullName(String firstName, String lastName) {
            
        }

        public String composeName(String firstName, String lastName) {
            return null;
        }
        
        // this is not a valid getter - should be ignored by the reflector
        public String getIgnored(String ignored) {
            return ignored;
        }
        
        @Override
        public String toString() {
            return "This is the TestClass";
        }
    }
}
