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

/**
 * 
 * @author Jeff Butler
 *
 */
public class SpringNamedParameterColumn<T> extends SqlColumn<T> {

    protected SpringNamedParameterColumn(SpringNamedParameterColumn<?> column) {
        super(column);
    }
    
    protected SpringNamedParameterColumn(String name, JDBCType jdbcType) {
        super(name, jdbcType);
    }
    
    @Override
    public <S> SpringNamedParameterColumn<S> inTable(SqlTable table) {
        SpringNamedParameterColumn<S> column = new SpringNamedParameterColumn<>(this);
        column.table = table;
        return column;
    }
    
    @Override
    public <S> SpringNamedParameterColumn<S> descending() {
        SpringNamedParameterColumn<S> column = new SpringNamedParameterColumn<>(this);
        column.sortOrder = DESCENDING;
        return column;
    }

    @Override
    public <S> SpringNamedParameterColumn<S> withAlias(String alias) {
        SpringNamedParameterColumn<S> column = new SpringNamedParameterColumn<>(this);
        column.alias = alias;
        return column;
    }
    
    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return ":" + parameterName; //$NON-NLS-1$
    }

    public static <T> SpringNamedParameterColumn<T> of(String name, JDBCType jdbcType) {
        return new SpringNamedParameterColumn<>(name, jdbcType);
    }
}
