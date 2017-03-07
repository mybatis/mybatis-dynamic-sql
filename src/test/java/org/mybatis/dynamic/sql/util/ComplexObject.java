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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplexObject {

    int id;
    String description;
    private Map<String, String> names = new HashMap<>();
    InnerClass innerClass = new InnerClass();
    private InnerClass[] innerClasses = new InnerClass[2];
    private List<InnerClass> innerClassList = new ArrayList<>();
    private Map<String, InnerClass> innerClassMap = new HashMap<>();
    
    public ComplexObject() {
        id = 3;
        description = "Outer Class";
        names.put("Fred", "Flintstone");
        innerClasses[0] = new InnerClass();
        innerClasses[1] = new InnerClass();
        innerClassList.add(new InnerClass());
        innerClassList.add(new InnerClass());
        innerClassList.add(new InnerClass());
        innerClassMap.put("first", new InnerClass());
        innerClassMap.put("second", new InnerClass());
    }
    
    public List<InnerClass> getInnerClassList() {
        return innerClassList;
    }

    public Map<String, InnerClass> getInnerClassMap() {
        return innerClassMap;
    }
    
    public boolean isBooleanProperty() {
        return false;
    }
    
    // should be ignored by the reflector
    public boolean is() {
        return false;
    }

    // should be ignored by the reflector
    public String get() {
        return null;
    }
    
    // should be ignored by the reflector
    public void getNull() {
    }
    
    public boolean isB() {
        return true;
    }
    
    // should be ignored by the reflector
    public String isC() {
        return null;
    }
    
    // should be ignored by the reflector
    public boolean isD(String name) {
        return true;
    }
    
    public boolean getBooleanProperty() {
        return true;
    }
    
    public String getThrowsException() {
        throw new RuntimeException();
    }
    
    public static class InnerClass {
        int id;
        String description;
        private Map<String, String> names = new HashMap<>();
        private int[] ints = new int[2];
        private boolean isActive = true;
        private String firstName = "Bamm Bamm";
        
        public InnerClass() {
            id = 33;
            description = "Inner Class";
            names.put("Barney", "Rubble");
            ints[0] = 44;
            ints[1] = 55;
        }

        public boolean isActive() {
            return isActive;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getA() {
            return "a";
        }

        public String retrieveB() {
            return "b";
        }
    }
}
