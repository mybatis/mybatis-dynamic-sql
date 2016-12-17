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
package org.mybatis.qbe.mybatis3;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.SqlTable;

/**
 * 
 * @author Jeff Butler
 *
 */
public class MyBatis3Field<T> extends SqlField<T> {

    protected String typeHandler;
    
    protected MyBatis3Field(String name, JDBCType jdbcType) {
        super(name, jdbcType);
    }
    
    public Optional<String> typeHandler() {
        return Optional.ofNullable(typeHandler);
    }
    
    public <S> MyBatis3Field<S> withTypeHandler(String typeHandler) {
        MyBatis3Field<S> field = MyBatis3Field.of(name, jdbcType);
        field.table = table;
        field.sortOrder = sortOrder;
        field.typeHandler = typeHandler;
        return field;
    }
    
    @Override
    public <S> MyBatis3Field<S> inTable(SqlTable table) {
        MyBatis3Field<S> field = MyBatis3Field.of(name, jdbcType);
        field.table = table;
        field.sortOrder = sortOrder;
        field.typeHandler = typeHandler;
        return field;
    }
    
    @Override
    public <S> MyBatis3Field<S> descending() {
        MyBatis3Field<S> field = MyBatis3Field.of(name, jdbcType);
        field.table = table;
        field.sortOrder = DESCENDING;
        field.typeHandler = typeHandler;
        return field;
    }

    @Override
    public String getFormattedJdbcPlaceholder(String parameterName) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#{"); //$NON-NLS-1$
        buffer.append(parameterName);
        buffer.append(",jdbcType="); //$NON-NLS-1$
        buffer.append(jdbcType().getName());
        
        typeHandler().ifPresent(th -> {
            buffer.append(",typeHandler="); //$NON-NLS-1$
            buffer.append(th);
        });
        
        buffer.append('}');
        return buffer.toString();
    }

    public static <T> MyBatis3Field<T> of(String name, JDBCType jdbcType) {
        return new MyBatis3Field<>(name, jdbcType);
    }
}
