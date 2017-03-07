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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.util.CustomCollectors;
import org.mybatis.ibatis.reflection.invoker.GetFieldInvoker;
import org.mybatis.ibatis.reflection.invoker.Invoker;
import org.mybatis.ibatis.reflection.invoker.MethodInvoker;

/**
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 *
 * @author Clinton Begin (initial work)
 * @author Jeff Butler (derivation)
 */
public class Reflector {

    private static final Logger log = Logger.getLogger(Reflector.class.getName());
    private static final String IS = "is"; //$NON-NLS-1$
    private static final String GET = "get"; //$NON-NLS-1$
    
    private Map<String, Invoker> getMethods = new HashMap<>();

    public Reflector(Class<?> clazz) {
        addGetMethods(clazz);
        addFields(clazz);
    }
    
    private void addGetMethods(Class<?> cls) {
        Map<String, Method> uniqueMethods = getClassMethods(cls);

        uniqueMethods.values().stream()
                .filter(this::isAnIsGetter)
                .collect(Collectors.toMap(this::extractIsPropertyName, MethodInvoker::new, (m1, m2) -> m1, () -> getMethods));
        
        uniqueMethods.values().stream()
                .filter(this::isAGetter)
                .collect(Collectors.toMap(this::extractGetPropertyName, MethodInvoker::new, (m1, m2) -> m1, () -> getMethods));
    }

    private boolean isAnIsGetter(Method method) {
        String methodName = method.getName();
        return methodName.startsWith(IS)
                && methodName.length() > IS.length()
                && method.getParameterTypes().length == 0
                && method.getReturnType().equals(boolean.class);
    }
    
    private String extractIsPropertyName(Method method) {
        String propertyName = method.getName().substring(IS.length());
        return fixPropertyCase(propertyName);
    }
    
    private boolean isAGetter(Method method) {
        String methodName = method.getName();
        return methodName.startsWith(GET)
                && methodName.length() > GET.length()
                && method.getParameterTypes().length == 0
                && !method.getReturnType().equals(void.class);
    }
    
    private String extractGetPropertyName(Method method) {
        String propertyName = method.getName().substring(GET.length());
        return fixPropertyCase(propertyName);
    }
    
    static String fixPropertyCase(String propertyName) {
        String fixedPropertyName = propertyName;
        if (propertyName.length() == 1 || (propertyName.length() > 1 && !Character.isUpperCase(propertyName.charAt(1)))) {
            fixedPropertyName = propertyName.substring(0, 1).toLowerCase(Locale.ENGLISH) + propertyName.substring(1);
        }

        return fixedPropertyName;
    }
    
    private void addFields(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredFields())
        .filter(this::hasNoGetterMethod)
        .filter(this::isValidPropertyName)
        .map(this::setAccessible)
        .filter(Field::isAccessible)
        .collect(Collectors.toMap(Field::getName, GetFieldInvoker::new,  (f1, f2) -> f1, () -> getMethods));
        
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }
    
    private boolean hasNoGetterMethod(Field field) {
        return !getMethods.containsKey(field.getName());
    }

    private boolean isValidPropertyName(Field field) {
        return isValidPropertyName(field.getName());
    }
    
    static boolean isValidPropertyName(String name) {
        return !name.isEmpty()
                && !name.startsWith("$") //$NON-NLS-1$
                && !"serialVersionUID".equals(name) //$NON-NLS-1$
                && ! "class".equals(name); //$NON-NLS-1$
    }

    /*
     * This method returns a map containing all methods declared in this
     * class and any superclass. We use this method, instead of the simpler
     * Class.getMethods(), because we want to look for private methods as well.
     *
     * @param clazz The class
     * 
     * @return A map containing all methods in this class
     */
    private Map<String, Method> getClassMethods(Class<?> clazz) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        
        addUniqueMethods(uniqueMethods, clazz.getDeclaredMethods());
        
        // we also need to look for interface methods -
        // because the class may be abstract
        Arrays.stream(clazz.getInterfaces())
        .forEach(i -> addUniqueMethods(uniqueMethods, i.getMethods()));

        if (clazz.getSuperclass() != null) {
            uniqueMethods.putAll(getClassMethods(clazz.getSuperclass()));
        }
        
        return uniqueMethods;
    }

    void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        Arrays.stream(methods)
        .filter(m -> !m.isBridge())
        .map(this::setAccessible)
        .filter(Method::isAccessible)
        .collect(Collectors.toMap(this::getMethodSignature, m -> m, (m1, m2) -> m1, () -> uniqueMethods));
    }
    
    private Method setAccessible(Method method) {
        try {
            method.setAccessible(true);
        } catch (Exception e) {
            // Ignored. If the method isn't accessible, it will be filtered out later in the pipeline.
            log.log(Level.FINEST, "Exception making method " + method.toString() + " accessible.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return method;
    }

    private Field setAccessible(Field field) {
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            // Ignored. If the method isn't accessible, it will be filtered out later in the pipeline.
            log.log(Level.FINEST, "Exception making field " + field.toString() + " accessible.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return field;
    }
    
    String getMethodSignature(Method method) {
        return method.getReturnType().getName()
                + "#" //$NON-NLS-1$
                + method.getName()
                + Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(CustomCollectors.joining(",", ":", "", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    
    public Optional<Invoker> getGetInvoker(String propertyName) {
        return Optional.ofNullable(getMethods.get(propertyName));
    }
}
