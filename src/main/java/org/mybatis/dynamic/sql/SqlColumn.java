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
 *     it is used by the compiler to match columns with conditions so it should
 *     not be removed.
*/
public class SqlColumn<T> implements SelectListItem {
    
    protected String name;
    protected SqlTable table;
    protected JDBCType jdbcType;
    protected boolean isDescending = false;
    protected Optional<String> alias = Optional.empty();
    protected Optional<String> typeHandler = Optional.empty();
    
    protected SqlColumn(SqlColumn<?> sqlColumn) {
        this.name = sqlColumn.name;
        this.table = sqlColumn.table;
        this.jdbcType = sqlColumn.jdbcType;
        this.isDescending = sqlColumn.isDescending;
        this.alias = sqlColumn.alias;
        this.typeHandler = sqlColumn.typeHandler;
    }
    
    protected SqlColumn(SqlTable table, String name, JDBCType jdbcType) {
        this.table = table;
        this.name = name;
        this.jdbcType = jdbcType;
    }
    
    public String name() {
        return name;
    }
    
    public JDBCType jdbcType() {
        return jdbcType;
    }

    @Override
    public Optional<SqlTable> table() {
        return Optional.of(table);
    }
    
    @Override
    public Optional<String> alias() {
        return alias;
    }
    
    public Optional<String> typeHandler() {
        return typeHandler;
    }
    
    public <S> SqlColumn<S> descending() {
        SqlColumn<S> column = new SqlColumn<>(this);
        column.isDescending = true;
        return column;
    }
    
    public SqlColumn<T> as(String alias) {
        SqlColumn<T> column = new SqlColumn<>(this);
        column.alias = Optional.of(alias);
        return column;
    }
    
    public <S> SqlColumn<S> withTypeHandler(String typeHandler) {
        SqlColumn<S> column = new SqlColumn<>(this);
        column.typeHandler = Optional.of(typeHandler);
        return column;
    }
    
    public boolean isDescending() {
        return isDescending;
    }
    
    @Override
    public String nameIncludingTableAlias(Optional<String> tableAlias) {
        return tableAlias.map(a -> a + "." + name()).orElse(name()); //$NON-NLS-1$
    }
    
    public static <T> SqlColumn<T> of(SqlTable table, String name, JDBCType jdbcType) {
        return new SqlColumn<>(table, name, jdbcType);
    }
}
