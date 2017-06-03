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

import java.util.Map;

import org.mybatis.dynamic.sql.reflection.property.PropertyTokenizer;
import org.mybatis.dynamic.sql.reflection.wrapper.BeanWrapper;
import org.mybatis.dynamic.sql.reflection.wrapper.MapWrapper;
import org.mybatis.dynamic.sql.reflection.wrapper.ObjectWrapper;

/**
 * Utility class that will retrieve a vale from a class.
 * 
 * @author Clinton Begin (initial work)
 * @author Jeff Butler (derivation)
 */
public class MetaObject {

    private ObjectWrapper objectWrapper;

    @SuppressWarnings("unchecked")
    private MetaObject(Object object) {
        if (object instanceof Map) {
            this.objectWrapper = new MapWrapper(this, (Map<String, Object>) object);
        } else {
            this.objectWrapper = new BeanWrapper(this, object);
        }
    }

    public static MetaObject forObject(Object object) {
        if (object == null) {
            return new NullMetaObject();
        } else {
            return new MetaObject(object);
        }
    }

    public Object getValue(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            return metaValue.getValue(prop.getChildren());
        } else {
            return objectWrapper.get(prop);
        }
    }

    public MetaObject metaObjectForProperty(String name) {
        Object value = getValue(name);
        return MetaObject.forObject(value);
    }

    private static class NullMetaObject extends MetaObject {
        private NullMetaObject() {
            super(NullMetaObject.class);
        }
        
        @Override
        public Object getValue(String name) {
            return null;
        }
    }
}
