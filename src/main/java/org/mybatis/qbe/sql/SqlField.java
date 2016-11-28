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
import org.mybatis.qbe.Renderer;

/**
 * 
 * @author Jeff Butler
 *
 */
public class SqlField<T> extends Field<T> {

    protected JDBCType jdbcType;
    protected String alias;
    
    protected SqlField(String name, JDBCType jdbcType, String alias) {
        super(name);
        this.jdbcType = jdbcType;
        this.alias = alias;
    }
    
    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        alias().ifPresent(a -> {
            sb.append(a);
            sb.append('.');
        });
        sb.append(super.name());
        return sb.toString();
    }
    
    public JDBCType jdbcType() {
        return jdbcType;
    }
    
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }
    
    public <S> SqlField<S> ignoringAlias() {
        return SqlField.of(name, jdbcType);
    }
    
    public <S> SqlField<S> withAlias(String alias) {
        return SqlField.of(name, jdbcType, alias);
    }
    
    @Override
    public Renderer getParameterRenderer(int parameterNumber) {
        return () -> "?"; //$NON-NLS-1$
    }
    
    public static <T> SqlField<T> of(String name, JDBCType jdbcType) {
        return SqlField.of(name, jdbcType, null);
    }
    
    public static <T> SqlField<T> of(String name, JDBCType jdbcType, String alias) {
        return new SqlField<>(name, jdbcType, alias);
    }
}
