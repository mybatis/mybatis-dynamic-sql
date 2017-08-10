package org.mybatis.dynamic.sql.render;

import java.util.Map;
import java.util.Optional;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class RenderingUtilities {

    public static String tableNameIncludingAlias(SqlTable table, Map<SqlTable, String> tableAliases) {
        return tableAlias(table, tableAliases)
                .map(a -> table.name() + " " + a) //$NON-NLS-1$
                .orElse(table.name());
    }
    
    public static String columnNameIncludingTableAlias(SqlColumn<?> column, Map<SqlTable, String> tableAliases) {
        return column.table()
                .map(t -> column.nameIncludingTableAlias(tableAlias(t, tableAliases)))
                .orElse(column.name());
    }
    
    private static Optional<String> tableAlias(SqlTable table, Map<SqlTable, String> tableAliases) {
        return Optional.ofNullable(tableAliases.get(table));
    }
}
