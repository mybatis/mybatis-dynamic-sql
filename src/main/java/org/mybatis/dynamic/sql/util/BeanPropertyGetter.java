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

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

public class BeanPropertyGetter {

    private static BeanPropertyGetter instance = new BeanPropertyGetter();
    private ObjectFactory objectFactory = new DefaultObjectFactory();
    private ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
    private ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    
    private BeanPropertyGetter() {
        super();
    }
    
    public static BeanPropertyGetter instance() {
        return instance;
    }
    
    /**
     * 
     * @param bean
     *            can be a bean or a map
     * @param property
     *            can be in the form obj.obj[].obj[]
     * @return
     */
    public Object getPropertyValue(Object bean, String property) {
        MetaObject metaObject = MetaObject.forObject(bean, objectFactory, objectWrapperFactory, reflectorFactory);
        return metaObject.getValue(property);
    }
}
