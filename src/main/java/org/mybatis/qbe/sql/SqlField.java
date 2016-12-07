/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.qbe.Field;

/**
 * 
 * @author Jeff Butler
 *
 */
public class SqlField<T> extends Field<T> {
    
    private static final SqlTable NULL_TABLE = SqlTable.of(""); //$NON-NLS-1$

    protected SqlTable table;
    protected JDBCType jdbcType;
    
    protected SqlField(String name, JDBCType jdbcType) {
        super(name);
        this.jdbcType = jdbcType;
    }
    
    public String nameIncludingTableAlias() {
        StringBuilder sb = new StringBuilder();
        alias().ifPresent(a -> {
            sb.append(a);
            sb.append('.');
        });
        sb.append(name());
        return sb.toString();
    }
    
    public String nameIgnoringTableAlias() {
        return name();
    }
    
    public JDBCType jdbcType() {
        return jdbcType;
    }

    public Optional<SqlTable> table() {
        return Optional.ofNullable(table);
    }
    
    public Optional<String> alias() {
        return table().orElse(NULL_TABLE).alias();
    }
    
    public <S> SqlField<S> inTable(SqlTable table) {
        SqlField<S> field = SqlField.of(name, jdbcType);
        field.table = table;
        return field;
    }
    
    public String getFormattedJdbcPlaceholder(String parameterName) {
        return String.format("{%s}", parameterName); //$NON-NLS-1$
    }
    
    public static <T> SqlField<T> of(String name, JDBCType jdbcType) {
        return new SqlField<>(name, jdbcType);
    }
}
