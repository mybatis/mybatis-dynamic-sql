package org.mybatis.dynamic.sqlserver;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sqlserver.select.function.Ascii;

public interface SQLServerBuilder {
	static Ascii ascii(BindableColumn<String> column) {
        return Ascii.of(column);
    }
}
