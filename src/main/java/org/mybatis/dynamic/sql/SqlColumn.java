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
package org.mybatis.dynamic.sql;

import java.sql.JDBCType;
import java.util.Optional;

/**
 * 
 * @author Jeff Butler
 *
 */
public class SqlColumn<T> extends AbstractColumn<T> {
    
    private static final String ASCENDING = "ASC"; //$NON-NLS-1$
    protected static final String DESCENDING = "DESC"; //$NON-NLS-1$
    private static final SqlTable NULL_TABLE = SqlTable.of(""); //$NON-NLS-1$

    protected SqlTable table;
    protected JDBCType jdbcType;
    protected String sortOrder = ASCENDING;
    protected String alias;
    
    protected SqlColumn(SqlColumn<?> sqlColumn) {
        super(sqlColumn.name);
        this.table = sqlColumn.table;
        this.jdbcType = sqlColumn.jdbcType;
        this.sortOrder = sqlColumn.sortOrder;
        this.alias = sqlColumn.alias;
    }
    
    protected SqlColumn(String name, JDBCType jdbcType) {
        super(name);
        this.jdbcType = jdbcType;
    }
    
    public String nameIncludingTableAlias() {
        StringBuilder sb = new StringBuilder();
        tableAlias().ifPresent(a -> {
            sb.append(a);
            sb.append('.');
        });
        sb.append(name());
        return sb.toString();
    }
    
    public String nameIncludingTableAndColumnAlias() {
        StringBuilder sb = new StringBuilder();
        sb.append(nameIncludingTableAlias());
        columnAlias().ifPresent(a -> {
            sb.append(" as "); //$NON-NLS-1$
            sb.append(a);
        });
        return sb.toString();
    }
    
    public JDBCType jdbcType() {
        return jdbcType;
    }

    public Optional<SqlTable> table() {
        return Optional.ofNullable(table);
    }
    
    public Optional<String> tableAlias() {
        return table().orElse(NULL_TABLE).alias();
    }
    
    public Optional<String> columnAlias() {
        return Optional.ofNullable(alias);
    }
    
    public <S> SqlColumn<S> inTable(SqlTable table) {
        SqlColumn<S> column = new SqlColumn<>(this);
        column.table = table;
        return column;
    }
    
    public <S> SqlColumn<S> descending() {
        SqlColumn<S> column = new SqlColumn<>(this);
        column.sortOrder = DESCENDING;
        return column;
    }
    
    public <S> SqlColumn<S> withAlias(String alias) {
        SqlColumn<S> column = new SqlColumn<>(this);
        column.alias = alias;
        return column;
    }
    
    public String getFormattedJdbcPlaceholder(String parameterName) {
        return String.format("{%s}", parameterName); //$NON-NLS-1$
    }
    
    public String orderByPhrase() {
        return String.format("%s %s", columnAlias().orElse(name()), sortOrder); //$NON-NLS-1$
    }
    
    public static <T> SqlColumn<T> of(String name, JDBCType jdbcType) {
        return new SqlColumn<>(name, jdbcType);
    }
}
