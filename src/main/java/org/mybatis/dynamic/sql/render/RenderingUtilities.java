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
package org.mybatis.dynamic.sql.render;

import java.util.Map;
import java.util.Optional;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public interface RenderingUtilities {

    static String tableNameIncludingAlias(SqlTable table, Map<SqlTable, String> tableAliases) {
        return tableAlias(table, tableAliases)
                .map(a -> table.name() + " " + a) //$NON-NLS-1$
                .orElseGet(table::name);
    }
    
    static String columnNameIncludingTableAlias(SqlColumn<?> column, Map<SqlTable, String> tableAliases) {
        return column.table()
                .map(t -> column.nameIncludingTableAlias(tableAlias(t, tableAliases)))
                .orElseGet(column::name);
    }
    
    static Optional<String> tableAlias(SqlTable table, Map<SqlTable, String> tableAliases) {
        return Optional.ofNullable(tableAliases.get(table));
    }
}
