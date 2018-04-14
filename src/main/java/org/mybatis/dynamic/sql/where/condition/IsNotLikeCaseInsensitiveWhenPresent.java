/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.where.condition;

import java.util.function.Supplier;

public class IsNotLikeCaseInsensitiveWhenPresent extends IsNotLikeCaseInsensitive {

    private boolean shouldRender;
    
    protected IsNotLikeCaseInsensitiveWhenPresent(Supplier<String> valueSupplier) {
        super(valueSupplier);
        shouldRender = valueSupplier.get() != null;
    }
    
    @Override
    public boolean shouldRender() {
        return shouldRender;
    }

    public static IsNotLikeCaseInsensitiveWhenPresent of(Supplier<String> valueSupplier) {
        return new IsNotLikeCaseInsensitiveWhenPresent(valueSupplier);
    }
}