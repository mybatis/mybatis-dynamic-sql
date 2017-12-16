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
package org.mybatis.dynamic.sql.where.condition;

import java.util.function.Supplier;

import org.mybatis.dynamic.sql.AbstractTwoValueCondition;

public class IsNotBetween<T> extends AbstractTwoValueCondition<T> {

    protected IsNotBetween(Builder<T> builder) {
        super(builder.valueSupplier1, builder.valueSupplier2);
    }
    
    @Override
    public String renderCondition(String columnName, String placeholder1, String placeholder2) {
        return columnName + " not between " + placeholder1 + " and " + placeholder2; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static class Builder<T> {
        private Supplier<T> valueSupplier1;
        private Supplier<T> valueSupplier2;
        
        private Builder(Supplier<T> valueSupplier1) {
            this.valueSupplier1 = valueSupplier1;
        }
        
        public IsNotBetween<T> and(Supplier<T> valueSupplier2) {
            this.valueSupplier2 = valueSupplier2;
            return new IsNotBetween<>(this);
        }
        
        public IsNotBetween<T> and(T value2) {
            return and(() -> value2);
        }
    }
    
    public static <T> Builder<T> isNotBetween(Supplier<T> valueSupplier1) {
        return new Builder<>(valueSupplier1);
    }
}
