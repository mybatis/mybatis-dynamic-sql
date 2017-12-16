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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mybatis.dynamic.sql.reflection.MetaObject;
import org.mybatis.dynamic.sql.reflection.Reflector;
import org.mybatis.dynamic.sql.reflection.ReflectorFactory;
import org.mybatis.dynamic.sql.reflection.invoker.Invoker;
import org.mybatis.dynamic.sql.reflection.property.PropertyTokenizer;

/**
 * Wrapper that retrieves values from a Java bean.
 * 
 * @author Clinton Begin (original work)
 * @author Jeff Butler (derivation)
 */
public class BeanWrapper extends ObjectWrapper {
    
    private static final Logger log = Logger.getLogger(BeanWrapper.class.getName());

    private Object object;
    private Reflector reflector;

    public BeanWrapper(MetaObject metaObject, Object object) {
        super(metaObject);
        this.object = object;
        reflector = ReflectorFactory.instance().findForClass(object.getClass());
    }

    @Override
    public Object get(PropertyTokenizer prop) {
        if (prop.getIndex() != null) {
            Object collection = resolveCollection(prop);
            return getCollectionValue(prop, collection);
        } else {
            return getBeanProperty(prop);
        }
    }

    private Object getBeanProperty(PropertyTokenizer prop) {
        return reflector.getGetInvoker(prop.getName())
                .map(this::invoke)
                .orElse(null);
    }
    
    private Object invoke(Invoker invoker) {
        try {
            return invoker.invoke(object, NO_ARGUMENTS);
        } catch (ReflectiveOperationException e) {
            log.log(Level.FINEST,
                    "Invoker exception while retrieving property value, returning null", e); //$NON-NLS-1$
            return null;
        }
    }
}
