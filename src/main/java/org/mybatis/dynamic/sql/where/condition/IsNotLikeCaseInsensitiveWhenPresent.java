/**
 *    Copyright 2016-2019 the original author or authors.
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

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class IsNotLikeCaseInsensitiveWhenPresent extends IsNotLikeCaseInsensitive {

    protected IsNotLikeCaseInsensitiveWhenPresent(Supplier<String> valueSupplier) {
        super(valueSupplier, Objects::nonNull);
    }
    
    public static IsNotLikeCaseInsensitiveWhenPresent of(Supplier<String> valueSupplier) {
        return new IsNotLikeCaseInsensitiveWhenPresent(valueSupplier);
    }

    @Override
    public IsNotLikeCaseInsensitiveWhenPresent then(UnaryOperator<String> transformer) {
        return shouldRender() ? new IsNotLikeCaseInsensitiveWhenPresent(() -> transformer.apply(value())) : this;
    }
}