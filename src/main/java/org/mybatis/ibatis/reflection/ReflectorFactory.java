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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReflectorFactory {
    private static ReflectorFactory instance = new ReflectorFactory();
    private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

    private ReflectorFactory() {
        super();
    }

    public static ReflectorFactory instance() {
        return instance;
    }
    
    public Reflector findForClass(Class<?> type) {
        Reflector cached = reflectorMap.get(type);
        if (cached == null) {
            cached = new Reflector(type);
            reflectorMap.put(type, cached);
        }
        return cached;
    }
}
