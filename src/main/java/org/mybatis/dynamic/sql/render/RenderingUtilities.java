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

import java.util.Optional;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public interface RenderingUtilities {
    static String nameIgnoringTableAlias(SqlColumn<?> column) {
        return column.name();
    }
    
    static String nameIncludingTableAlias(SqlColumn<?> column) {
        return tableAlias(column).map(a -> a + "." + column.name()).orElse(column.name()); //$NON-NLS-1$
    }

    static String nameIncludingTableAndColumnAlias(SqlColumn<?> column) {
        return column.alias().map(a -> nameIncludingTableAlias(column) + " as " + a) //$NON-NLS-1$
                .orElse(nameIncludingTableAlias(column));
    }

    static Optional<String> tableAlias(SqlColumn<?> column) {
        return column.table().flatMap(SqlTable::alias);
    }

    static String nameIncludingAlias(SqlTable table) {
        return table.alias().map(a -> table.name() + " " + a).orElse(table.name()); //$NON-NLS-1$
    }
}
