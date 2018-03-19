package org.mybatis.dynamic.mysql;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.mysql.select.function.Ascii;

public interface MySQLBuilder {
	static Ascii ascii(BindableColumn<String> column) {
        return Ascii.of(column);
    }
}
