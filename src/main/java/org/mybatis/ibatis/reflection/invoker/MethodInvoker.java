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
package org.mybatis.ibatis.reflection.invoker;

import java.lang.reflect.Method;

/**
 * @author Clinton Begin (initial work)
 * @author Jeff Butler (derivation)
 */
public class MethodInvoker implements Invoker {

    private Method method;

    public MethodInvoker(Method method) {
        this.method = method;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws ReflectiveOperationException {
        return method.invoke(target, args);
    }
    
    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }
}
