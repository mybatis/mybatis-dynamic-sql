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
    protected final boolean isDescending;
    protected final String alias;
    protected final String typeHandler;
    protected final RenderingStrategy renderingStrategy;
    protected final ParameterTypeConverter<T, ?> parameterTypeConverter;

    private SqlColumn(Builder<T> builder) {
        name = Objects.requireNonNull(builder.name);
        table = Objects.requireNonNull(builder.table);
        jdbcType = builder.jdbcType;
        isDescending = builder.isDescending;
        alias = builder.alias;
        typeHandler = builder.typeHandler;
        renderingStrategy = builder.renderingStrategy;
        parameterTypeConverter = builder.parameterTypeConverter;
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
        Builder<T> b = copy();
        return b.withDescending(true).build();
    }

    @Override
    public SqlColumn<T> as(String alias) {
        Builder<T> b = copy();
        return b.withAlias(alias).build();
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
        Builder<S> b = copy();
        return b.withTypeHandler(typeHandler).build();
    }

    @NotNull
    public <S> SqlColumn<S> withRenderingStrategy(RenderingStrategy renderingStrategy) {
        Builder<S> b = copy();
        return b.withRenderingStrategy(renderingStrategy).build();
    }

    @NotNull
    public <S> SqlColumn<S> withParameterTypeConverter(ParameterTypeConverter<S, ?> parameterTypeConverter) {
        Builder<S> b = copy();
        return b.withParameterTypeConverter(parameterTypeConverter).build();
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
    private <S> Builder<S> copy() {
        return new Builder<S>()
                .withName(this.name)
                .withTable(this.table)
                .withJdbcType(this.jdbcType)
                .withDescending(this.isDescending)
                .withAlias(this.alias)
                .withTypeHandler(this.typeHandler)
                .withRenderingStrategy((this.renderingStrategy))
                .withParameterTypeConverter((ParameterTypeConverter<S, ?>) this.parameterTypeConverter);
    }

    private String applyTableAlias(String tableAlias) {
        return tableAlias + "." + name(); //$NON-NLS-1$
    }

    public static <T> SqlColumn<T> of(String name, SqlTable table) {
        return new Builder<T>().withName(name)
                .withTable(table)
                .build();
    }

    public static <T> SqlColumn<T> of(String name, SqlTable table, JDBCType jdbcType) {
        return new Builder<T>().withName(name)
                .withTable(table)
                .withJdbcType(jdbcType)
                .build();
    }

    public static class Builder<T> {
        protected String name;
        protected SqlTable table;
        protected JDBCType jdbcType;
        protected boolean isDescending = false;
        protected String alias;
        protected String typeHandler;
        protected RenderingStrategy renderingStrategy;
        protected ParameterTypeConverter<T, ?> parameterTypeConverter;

        public Builder<T> withName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        public Builder<T> withJdbcType(JDBCType jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }

        public Builder<T> withDescending(boolean isDescending) {
            this.isDescending = isDescending;
            return this;
        }

        public Builder<T> withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder<T> withTypeHandler(String typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }

        public Builder<T> withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder<T> withParameterTypeConverter(ParameterTypeConverter<T, ?> parameterTypeConverter) {
            this.parameterTypeConverter = parameterTypeConverter;
            return this;
        }

        public SqlColumn<T> build() {
            return new SqlColumn<>(this);
        }
    }
}
