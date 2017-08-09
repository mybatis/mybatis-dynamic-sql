package org.mybatis.dynamic.sql.render;

import java.util.Optional;

import org.mybatis.dynamic.sql.SqlTable;

public class RenderingUtilities {

    public static String tableNameIncludingAlias(SqlTable table, Optional<String> tableAlias) {
        return tableAlias.map(a -> table.name() + " " + a).orElse(table.name()); //$NON-NLS-1$
    }
}
