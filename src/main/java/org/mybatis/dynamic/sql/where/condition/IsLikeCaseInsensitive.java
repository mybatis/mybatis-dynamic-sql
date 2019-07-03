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

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class IsLikeCaseInsensitive extends AbstractSingleValueCondition<String> {
    protected IsLikeCaseInsensitive(Supplier<String> valueSupplier) {
        super(valueSupplier);
    }
    
    protected IsLikeCaseInsensitive(Supplier<String> valueSupplier, Predicate<String> predicate) {
        super(valueSupplier, predicate);
    }
    
    @Override
    public String renderCondition(String columnName, String placeholder) {
        return "upper(" + columnName + ") like " + placeholder; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public String value() {
        return StringUtilities.safelyUpperCase(super.value());
    }

    public static IsLikeCaseInsensitive of(Supplier<String> valueSupplier) {
        return new IsLikeCaseInsensitive(valueSupplier);
    }
    
    public IsLikeCaseInsensitive when(Predicate<String> predicate) {
        return new IsLikeCaseInsensitive(valueSupplier, predicate);
    }

    public IsLikeCaseInsensitive then(UnaryOperator<String> transformer) {
        return shouldRender() ? new IsLikeCaseInsensitive(() -> transformer.apply(value())) : this;
    }
}