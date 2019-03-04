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
package org.mybatis.dynamic.sql.util;

import java.util.function.Supplier;

import org.mybatis.dynamic.sql.SqlColumn;

public class ValueMapping<T> extends AbstractColumnMapping implements UpdateMapping {

    private Supplier<T> valueSupplier;
    
    private ValueMapping(SqlColumn<T> column, Supplier<T> valueSupplier) {
        super(column);
        this.valueSupplier = valueSupplier;
    }
    
    public T value() {
        return valueSupplier.get();
    }

    @Override
    public <R> R accept(UpdateMappingVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static <T> ValueMapping<T> of(SqlColumn<T> column, Supplier<T> valueSupplier) {
        return new ValueMapping<>(column, valueSupplier);
    }
}
