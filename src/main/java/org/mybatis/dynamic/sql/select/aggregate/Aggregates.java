package org.mybatis.dynamic.sql.select.aggregate;

import org.mybatis.dynamic.sql.SqlColumn;

public interface Aggregates {

    static Count<Long> count() {
        return Count.count();
    }

    static <T> Count<T> count(SqlColumn<T> column) {
        return Count.count(column);
    }

    static <T> Max<T> max(SqlColumn<T> column) {
        return Max.max(column);
    }

    static <T> Min<T> min(SqlColumn<T> column) {
        return Min.min(column);
    }
}
