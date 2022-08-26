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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;

import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlColumn;

public class ColumnSortSpecification implements SortSpecification {
    private final String tableAlias;
    private final SqlColumn<?> column;
    private final boolean isDescending;

    public ColumnSortSpecification(String tableAlias, SqlColumn<?> column) {
        this(tableAlias, column, false);
    }

    private ColumnSortSpecification(String tableAlias, SqlColumn<?> column, boolean isDescending) {
        this.tableAlias = Objects.requireNonNull(tableAlias);
        this.column = Objects.requireNonNull(column);
        this.isDescending = isDescending;
    }

    @Override
    public SortSpecification descending() {
        return new ColumnSortSpecification(tableAlias, column, true);
    }

    @Override
    public String orderByName() {
        return tableAlias + "." + column.name(); //$NON-NLS-1$
    }

    @Override
    public boolean isDescending() {
        return isDescending;
    }
}
