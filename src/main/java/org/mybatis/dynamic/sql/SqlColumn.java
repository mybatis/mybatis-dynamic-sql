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
        return tableAlias().map(a -> a + "." + name()).orElse(name()); //$NON-NLS-1$
    }
    
    public String nameIncludingTableAndColumnAlias() {
        return columnAlias().map(a -> nameIncludingTableAlias() + " as " + a) //$NON-NLS-1$
                .orElse(nameIncludingTableAlias());
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
    
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return "{" + prefix + "." + parameterName + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public String orderByPhrase() {
        return columnAlias().orElseGet(this::name) + " " + sortOrder; //$NON-NLS-1$
    }
    
    public static <T> SqlColumn<T> of(String name, JDBCType jdbcType) {
        return new SqlColumn<>(name, jdbcType);
    }
}
