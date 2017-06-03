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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.reflection.invoker.GetFieldInvoker;
import org.mybatis.dynamic.sql.reflection.invoker.Invoker;
import org.mybatis.dynamic.sql.reflection.invoker.MethodInvoker;
import org.mybatis.dynamic.sql.util.CustomCollectors;

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
                .filter(Reflector::isAnIsGetter)
                .collect(Collectors.toMap(Reflector::extractIsPropertyName,
                        MethodInvoker::new, (m1, m2) -> m1, () -> getMethods));
        
        uniqueMethods.values().stream()
                .filter(Reflector::isAGetter)
                .collect(Collectors.toMap(Reflector::extractGetPropertyName,
                        MethodInvoker::new, (m1, m2) -> m1, () -> getMethods));
    }

    private static boolean isAnIsGetter(Method method) {
        String methodName = method.getName();
        return methodName.startsWith(IS)
                && methodName.length() > IS.length()
                && method.getParameterTypes().length == 0
                && method.getReturnType().equals(boolean.class);
    }
    
    private static String extractIsPropertyName(Method method) {
        String propertyName = method.getName().substring(IS.length());
        return fixPropertyCase(propertyName);
    }
    
    private static boolean isAGetter(Method method) {
        String methodName = method.getName();
        return methodName.startsWith(GET)
                && methodName.length() > GET.length()
                && method.getParameterTypes().length == 0
                && !method.getReturnType().equals(void.class);
    }
    
    private static String extractGetPropertyName(Method method) {
        String propertyName = method.getName().substring(GET.length());
        return fixPropertyCase(propertyName);
    }
    
    static String fixPropertyCase(String propertyName) {
        String fixedPropertyName = propertyName;
        if (propertyShouldBeLowerCased(propertyName)) {
            fixedPropertyName = propertyName.substring(0, 1).toLowerCase(Locale.ENGLISH) + propertyName.substring(1);
        }

        return fixedPropertyName;
    }
    
    private static boolean propertyShouldBeLowerCased(String propertyName) {
        return propertyName.length() == 1
                || propertyName.length() > 1 && !Character.isUpperCase(propertyName.charAt(1));
    }
    
    private void addFields(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredFields())
            .filter(this::hasNoGetterMethod)
            .filter(Reflector::isValidPropertyName)
            .map(Reflector::setAccessible)
            .filter(Reflector::isAccessible)
            .collect(Collectors.toMap(Field::getName, GetFieldInvoker::new,  (f1, f2) -> f1, () -> getMethods));
        
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }
    
    private boolean hasNoGetterMethod(Field field) {
        return !getMethods.containsKey(field.getName());
    }

    private static boolean isValidPropertyName(Field field) {
        return isValidPropertyName(field.getName());
    }
    
    static boolean isValidPropertyName(String name) {
        return !name.isEmpty()
                && !name.startsWith("$") //$NON-NLS-1$
                && !"serialVersionUID".equals(name) //$NON-NLS-1$
                && ! "class".equals(name); //$NON-NLS-1$
    }

    private static boolean isAccessible(Field field) {
        return field.isAccessible() || Modifier.isPublic(field.getModifiers());
    }
    
    private static boolean isAccessible(Method method) {
        return method.isAccessible() || Modifier.isPublic(method.getModifiers());
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
    private static Map<String, Method> getClassMethods(Class<?> clazz) {
        // build map of all class methods (including private methods if they can be made accessible) 
        Map<String, Method> uniqueMethods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> !m.isBridge())
                .map(Reflector::setAccessible)
                .filter(Reflector::isAccessible)
                .collect(Collectors.toMap(Reflector::getMethodSignature, Function.identity()));
        
        // add interface methods because the class may be abstract
        Arrays.stream(clazz.getInterfaces())
                .map(Class::getMethods)
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(Reflector::getMethodSignature,
                        Function.identity(), (m1, m2) -> m1, () -> uniqueMethods));
        
        // add methods from the superclass if there is one
        if (clazz.getSuperclass() != null) {
            getClassMethods(clazz.getSuperclass()).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (m1, m2) -> m1, () -> uniqueMethods));
        }
        
        return uniqueMethods;
    }
    
    private static Method setAccessible(Method method) {
        try {
            method.setAccessible(true);
        } catch (Exception e) {
            // Ignored. If the method isn't accessible, it will be filtered out later in the pipeline.
            log.log(Level.FINEST,
                    "Exception making method " + method.toString() + " accessible.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return method;
    }

    private static Field setAccessible(Field field) {
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            // Ignored. If the method isn't accessible, it will be filtered out later in the pipeline.
            log.log(Level.FINEST,
                    "Exception making field " + field.toString() + " accessible.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return field;
    }
    
    static String getMethodSignature(Method method) {
        return method.getReturnType().getName()
                + "#" //$NON-NLS-1$
                + method.getName()
                + Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(CustomCollectors.joining(",", ":", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public Optional<Invoker> getGetInvoker(String propertyName) {
        return Optional.ofNullable(getMethods.get(propertyName));
    }
}
