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
package org.mybatis.dynamic.sql.util;

import java.util.Objects;

import org.mybatis.dynamic.sql.SqlColumn;

public abstract class AbstractColumnMapping {
    protected final SqlColumn<?> column;

    protected AbstractColumnMapping(SqlColumn<?> column) {
        this.column = Objects.requireNonNull(column);
    }

    public String columnName() {
        return column.name();
    }

    @SuppressWarnings("java:S1452")
    public SqlColumn<?> column() {
        return column;
    }

    public abstract <R> R accept(ColumnMappingVisitor<R> visitor);
}
