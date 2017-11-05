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
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.TableAliasCalculator;

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
    protected Optional<String> typeHandler;
    
    private SqlColumn(Builder builder) {
        this.name = Objects.requireNonNull(builder.name);
        this.jdbcType = Objects.requireNonNull(builder.jdbcType);
        this.table = Objects.requireNonNull(builder.table);
        this.typeHandler = Optional.ofNullable(builder.typeHandler);
    }
    
    protected SqlColumn(SqlColumn<?> sqlColumn) {
        this.name = sqlColumn.name;
        this.table = sqlColumn.table;
        this.jdbcType = sqlColumn.jdbcType;
        this.isDescending = sqlColumn.isDescending;
        this.alias = sqlColumn.alias;
        this.typeHandler = sqlColumn.typeHandler;
    }
    
    public String name() {
        return name;
    }
    
    public JDBCType jdbcType() {
        return jdbcType;
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }
    
    public Optional<String> typeHandler() {
        return typeHandler;
    }
    
    public SqlColumn<T> descending() {
        SqlColumn<T> column = new SqlColumn<>(this);
        column.isDescending = true;
        return column;
    }
    
    public SqlColumn<T> as(String alias) {
        SqlColumn<T> column = new SqlColumn<>(this);
        column.alias = Optional.of(alias);
        return column;
    }
    
    public boolean isDescending() {
        return isDescending;
    }
    
    @Override
    public String applyTableAliasToName(TableAliasCalculator tableAliasCalculator) {
        return tableAliasCalculator.aliasForColumn(table)
                .map(this::applyTableAlias)
                .orElseGet(this::name);
    }
    
    private String applyTableAlias(String tableAlias) {
        return tableAlias + "." + name(); //$NON-NLS-1$
    }
    
    public static class Builder {
        private SqlTable table;
        private String name;
        private JDBCType jdbcType;
        private String typeHandler;
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder withJdbcType(JDBCType jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }
        
        public Builder withTypeHandler(String typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }
        
        public <T> SqlColumn<T> build() {
            return new SqlColumn<>(this);
        }
    }
}
