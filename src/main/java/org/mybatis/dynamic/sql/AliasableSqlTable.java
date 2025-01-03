/*
 *    Copyright 2016-2025 the original author or authors.
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

import org.jspecify.annotations.Nullable;

public abstract class AliasableSqlTable<T extends AliasableSqlTable<T>> extends SqlTable {

    private @Nullable String tableAlias;
    private final Supplier<T> constructor;

    protected AliasableSqlTable(String tableName, Supplier<T> constructor) {
        super(tableName);
        this.constructor = Objects.requireNonNull(constructor);
    }

    public T withAlias(String alias) {
        T newTable = constructor.get();
        ((AliasableSqlTable<T>) newTable).tableAlias = alias;
        newTable.tableName = tableName;
        return newTable;
    }

    /**
     * Returns a new instance of this table with the specified name. All column instances are recreated.
     * This is useful for sharding where the table name may change at runtime based on some sharding algorithm,
     * but all other table attributes are the same.
     *
     * @param name new name for the table
     * @return a new AliasableSqlTable with the specified name, all other table attributes are copied
     */
    public T withName(String name) {
        Objects.requireNonNull(name);
        T newTable = constructor.get();
        ((AliasableSqlTable<T>) newTable).tableAlias = tableAlias;
        newTable.tableName = name;
        return newTable;
    }

    @Override
    public Optional<String> tableAlias() {
        return Optional.ofNullable(tableAlias);
    }
}
