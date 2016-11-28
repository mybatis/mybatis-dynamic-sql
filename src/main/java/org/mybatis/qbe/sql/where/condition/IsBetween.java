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
package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.BaseTwoValueCondition;
import org.mybatis.qbe.Renderer;

public class IsBetween<T> extends BaseTwoValueCondition<T> {

    protected IsBetween(T value1, T value2) {
        super(value1, value2);
    }
    
    @Override
    public String render(Renderer parameterRenderer1, Renderer parameterRenderer2) {
        return String.format("between %s and %s", //$NON-NLS-1$
                parameterRenderer1.render(),
                parameterRenderer2.render());
    }

    public static class Builder<T> {
        private T value1;
        
        private Builder(T value1) {
            this.value1 = value1;
        }
        
        public IsBetween<T> and(T value2) {
            return new IsBetween<>(value1, value2);
        }
        
        public static <T> Builder<T> of(T value1) {
            return new Builder<>(value1);
        }
    }
}
