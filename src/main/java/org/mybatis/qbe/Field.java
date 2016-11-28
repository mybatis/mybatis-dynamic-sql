/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe;

/**
 * 
 * @author Jeff Butler
 *
 * @param <T> - even though the type is not directly used in this class,
 *  it is used by the compiler to match fields with conditions so it should
 *  not be removed.
 */
public abstract class Field<T> {

    protected String name;
    
    protected Field(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }

    /**
     * This method provides an opportunity to alter the field name on the left side of the
     * expression if required for any reason (such as when there is an SQL table alias).
     * 
     * @return
     */
    public abstract String render();
    
    /**
     * This returns the value on the "right side" of the expression.
     * 
     * @param parameterNumber
     * @return
     */
    public abstract Renderer getParameterRenderer(int parameterNumber);
}
