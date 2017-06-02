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
 * @param <T> - even though the type is not directly used in this class,
 *  it is used by the compiler to match columns with conditions so it should
 *  not be removed.
*/
public class SqlColumn<T> {
    
    private static final String ASCENDING = "ASC"; //$NON-NLS-1$
    protected static final String DESCENDING = "DESC"; //$NON-NLS-1$

    protected String name;
    protected SqlTable table;
    protected JDBCType jdbcType;
    protected String sortOrder = ASCENDING;
    protected String alias;
    protected String typeHandler;
    
    protected SqlColumn(SqlColumn<?> sqlColumn) {
        this.name = sqlColumn.name;
        this.table = sqlColumn.table;
        this.jdbcType = sqlColumn.jdbcType;
        this.sortOrder = sqlColumn.sortOrder;
        this.alias = sqlColumn.alias;
        this.typeHandler = sqlColumn.typeHandler;
    }
    
    protected SqlColumn(String name, JDBCType jdbcType) {
        this.name = name;
        this.jdbcType = jdbcType;
    }
    
    public String name() {
        return name;
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
        return table().map(SqlTable::alias).orElse(Optional.empty());
    }
    
    public Optional<String> columnAlias() {
        return Optional.ofNullable(alias);
    }
    
    public Optional<String> typeHandler() {
        return Optional.ofNullable(typeHandler);
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
    
    public <S> SqlColumn<S> withTypeHandler(String typeHandler) {
        SqlColumn<S> column = new SqlColumn<>(this);
        column.typeHandler = typeHandler;
        return column;
    }
    
    public String sortOrder() {
        return sortOrder;
    }
    
    public static <T> SqlColumn<T> of(String name, JDBCType jdbcType) {
        return new SqlColumn<>(name, jdbcType);
    }
}
