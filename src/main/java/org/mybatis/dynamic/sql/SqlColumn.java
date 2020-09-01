/*
 *    Copyright 2016-2020 the original author or authors.
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

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class SqlColumn<T> implements BindableColumn<T>, SortSpecification {
    
    protected final String name;
    protected final SqlTable table;
    protected final JDBCType jdbcType;
    protected boolean isDescending = false;
    protected String alias;
    protected String typeHandler;
    protected RenderingStrategy renderingStrategy;
    protected ParameterTypeConverter<T, ?> parameterTypeConverter;

    private SqlColumn(Builder builder) {
        name = Objects.requireNonNull(builder.name);
        jdbcType = builder.jdbcType;
        table = Objects.requireNonNull(builder.table);
        typeHandler = builder.typeHandler;
    }

    protected SqlColumn(SqlColumn<T> sqlColumn) {
        name = sqlColumn.name;
        table = sqlColumn.table;
        jdbcType = sqlColumn.jdbcType;
        isDescending = sqlColumn.isDescending;
        alias = sqlColumn.alias;
        typeHandler = sqlColumn.typeHandler;
        renderingStrategy = sqlColumn.renderingStrategy;
        parameterTypeConverter = sqlColumn.parameterTypeConverter;
    }
    
    public String name() {
        return name;
    }
    
    public SqlTable table() {
        return table;
    }
    
    @Override
    public Optional<JDBCType> jdbcType() {
        return Optional.ofNullable(jdbcType);
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }
    
    @Override
    public Optional<String> typeHandler() {
        return Optional.ofNullable(typeHandler);
    }
    
    @Override
    public Object convertParameterType(T value) {
        return parameterTypeConverter == null ? value : parameterTypeConverter.convert(value);
    }
    
    @Override
    public SortSpecification descending() {
        SqlColumn<T> column = new SqlColumn<>(this);
        column.isDescending = true;
        return column;
    }
    
    @Override
    public SqlColumn<T> as(String alias) {
        SqlColumn<T> column = new SqlColumn<>(this);
        column.alias = alias;
        return column;
    }
    
    @Override
    public boolean isDescending() {
        return isDescending;
    }
    
    @Override
    public String aliasOrName() {
        return alias().orElse(name);
    }
    
    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return tableAliasCalculator.aliasForColumn(table)
                .map(this::applyTableAlias)
                .orElseGet(this::name);
    }
    
    @Override
    public Optional<RenderingStrategy> renderingStrategy() {
        return Optional.ofNullable(renderingStrategy);
    }

    @NotNull
    public <S> SqlColumn<S> withTypeHandler(String typeHandler) {
        SqlColumn<S> column = copy();
        column.typeHandler = typeHandler;
        return column;
    }

    @NotNull
    public <S> SqlColumn<S> withRenderingStrategy(RenderingStrategy renderingStrategy) {
        SqlColumn<S> column = copy();
        column.renderingStrategy = renderingStrategy;
        return column;
    }

    @NotNull
    public <S> SqlColumn<S> withParameterTypeConverter(ParameterTypeConverter<S, ?> parameterTypeConverter) {
        SqlColumn<S> column = copy();
        column.parameterTypeConverter = parameterTypeConverter;
        return column;
    }

    /**
     * This method helps us tell a bit of fiction to the Java compiler. Java, for better or worse,
     * does not carry generic type information through chained methods. We want to enable method
     * chaining in the "with" methods. With this bit of fiction, we force the compiler to delay type
     * inference to the last method in the chain.
     *
     * @param <S> the type. Will be the same as T for this usage.
     * @return a new SqlColumn of type S (S is the same as T)
     */
    @SuppressWarnings("unchecked")
    private <S> SqlColumn<S> copy() {
        return new SqlColumn<>((SqlColumn<S>) this);
    }

    private String applyTableAlias(String tableAlias) {
        return tableAlias + "." + name(); //$NON-NLS-1$
    }
    
    public static <T> SqlColumn<T> of(String name, SqlTable table) {
        return SqlColumn.withName(name)
                .withTable(table)
                .build();
    }
    
    public static <T> SqlColumn<T> of(String name, SqlTable table, JDBCType jdbcType) {
        return SqlColumn.withName(name)
                .withTable(table)
                .withJdbcType(jdbcType)
                .build();
    }
    
    public static Builder withName(String name) {
        return new Builder().withName(name);
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
