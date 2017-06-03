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
package org.mybatis.dynamic.sql.reflection.wrapper;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import org.mybatis.dynamic.sql.reflection.MetaObject;
import org.mybatis.dynamic.sql.reflection.ReflectionException;
import org.mybatis.dynamic.sql.reflection.property.PropertyTokenizer;

/**
 * Base class of wrappers (classes that will retrieve values). 
 * 
 * @author Clinton Begin (initial work)
 * @author Jeff Butler (derivation)
 */
public abstract class ObjectWrapper {

    protected static final Object[] NO_ARGUMENTS = new Object[0];
    protected MetaObject metaObject;

    protected ObjectWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    protected Object resolveCollection(PropertyTokenizer prop) {
        return metaObject.getValue(prop.getName());
    }

    protected Object getCollectionValue(PropertyTokenizer prop, Object collection) {
        if (collection instanceof Map) {
            return ((Map<?, ?>) collection).get(prop.getIndex());
        } else {
            int i = Integer.parseInt(prop.getIndex());
            if (collection instanceof List) {
                return ((List<?>) collection).get(i);
            } else if (collection.getClass().isArray()) {
                return Array.get(collection, i);
            } else {
                throw new ReflectionException(
                        "The '" + prop.getName() + "' property of " //$NON-NLS-1$ //$NON-NLS-2$
                                + collection + " is not a List or Array."); //$NON-NLS-1$
            }
        }
    }

    public abstract Object get(PropertyTokenizer prop);
}
