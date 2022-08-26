/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class AliasableSqlTable<T extends AliasableSqlTable<T>> extends SqlTable {

    private String tableAlias;
    private final Supplier<T> constructor;

    protected AliasableSqlTable(String tableName, Supplier<T> constructor) {
        super(tableName);
        this.constructor = Objects.requireNonNull(constructor);
    }

    protected AliasableSqlTable(Supplier<String> tableNameSupplier, Supplier<T> constructor) {
        super(tableNameSupplier);
        this.constructor = Objects.requireNonNull(constructor);
    }

    public T withAlias(String alias) {
        T newTable = constructor.get();
        ((AliasableSqlTable<T>) newTable).tableAlias = alias;
        return newTable;
    }

    @Override
    public Optional<String> tableAlias() {
        return Optional.ofNullable(tableAlias);
    }
}
