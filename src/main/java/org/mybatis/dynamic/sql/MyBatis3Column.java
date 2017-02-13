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
public class MyBatis3Column<T> extends SqlColumn<T> {

    protected String typeHandler;
    
    protected MyBatis3Column(MyBatis3Column<?> myBatis3Column) {
        super(myBatis3Column);
        this.typeHandler = myBatis3Column.typeHandler;
    }
    
    protected MyBatis3Column(String name, JDBCType jdbcType) {
        super(name, jdbcType);
    }
    
    public Optional<String> typeHandler() {
        return Optional.ofNullable(typeHandler);
    }
    
    public <S> MyBatis3Column<S> withTypeHandler(String typeHandler) {
        MyBatis3Column<S> column = new MyBatis3Column<>(this);
        column.typeHandler = typeHandler;
        return column;
    }
    
    @Override
    public <S> MyBatis3Column<S> inTable(SqlTable table) {
        MyBatis3Column<S> column = new MyBatis3Column<>(this);
        column.table = table;
        return column;
    }
    
    @Override
    public <S> MyBatis3Column<S> descending() {
        MyBatis3Column<S> column = new MyBatis3Column<>(this);
        column.sortOrder = DESCENDING;
        return column;
    }

    @Override
    public <S> MyBatis3Column<S> withAlias(String alias) {
        MyBatis3Column<S> column = new MyBatis3Column<>(this);
        column.alias = alias;
        return column;
    }
    
    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#{"); //$NON-NLS-1$
        buffer.append(prefix);
        buffer.append('.');
        buffer.append(parameterName);
        buffer.append(",jdbcType="); //$NON-NLS-1$
        buffer.append(jdbcType().getName());
        buffer.append(typeHandler().map(th -> ",typeHandler=" + th) //$NON-NLS-1$
                .orElse("")); //$NON-NLS-1$
        buffer.append('}');
        return buffer.toString();
    }

    public static <T> MyBatis3Column<T> of(String name, JDBCType jdbcType) {
        return new MyBatis3Column<>(name, jdbcType);
    }
}
