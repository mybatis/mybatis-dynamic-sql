/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class SqlColumn<T> implements BindableColumn<T>, SortSpecification {

    protected final String name;
    protected final SqlTable table;
    protected final @Nullable JDBCType jdbcType;
    protected final String descendingPhrase;
    protected final @Nullable String alias;
    protected final @Nullable String typeHandler;
    protected final @Nullable RenderingStrategy renderingStrategy;
    protected final ParameterTypeConverter<T, ?> parameterTypeConverter;
    protected final @Nullable String tableQualifier;
    protected final @Nullable Class<T> javaType;

    private SqlColumn(Builder<T> builder) {
        name = Objects.requireNonNull(builder.name);
        table = Objects.requireNonNull(builder.table);
        jdbcType = builder.jdbcType;
        descendingPhrase = builder.descendingPhrase;
        alias = builder.alias;
        typeHandler = builder.typeHandler;
        renderingStrategy = builder.renderingStrategy;
        parameterTypeConverter = Objects.requireNonNull(builder.parameterTypeConverter);
        tableQualifier = builder.tableQualifier;
        javaType = builder.javaType;
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
    public Optional<Class<T>> javaType() {
        return Optional.ofNullable(javaType);
    }

    @Override
    public @Nullable Object convertParameterType(@Nullable T value) {
        return parameterTypeConverter.convert(value);
    }

    @Override
    public SortSpecification descending() {
        Builder<T> b = copy();
        return b.withDescendingPhrase(" DESC").build(); //$NON-NLS-1$
    }

    @Override
    public SqlColumn<T> as(String alias) {
        Builder<T> b = copy();
        return b.withAlias(alias).build();
    }

    /**
     * Override the calculated table qualifier if there is one. This is useful for sub-queries
     * where the calculated table qualifier may not be correct in all cases.
     *
     * @param tableQualifier the table qualifier to apply to the rendered column name
     * @return a new column that will be rendered with the specified table qualifier
     */
    public SqlColumn<T> qualifiedWith(String tableQualifier) {
        Builder<T> b = copy();
        b.withTableQualifier(tableQualifier);
        return b.build();
    }

    /**
     * Set an alias with a camel cased string based on the column name. This can be useful for queries using
     * the {@link org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper} where the columns are placed into
     * a map based on the column name returned from the database.
     *
     * <p>A camel case string is mixed case, and most databases do not support unquoted mixed case strings
     * as identifiers. Therefore, the generated alias will be surrounded by double quotes thereby making it a
     * quoted identifier. Most databases will respect quoted mixed case identifiers.
     *
     * @return a new column aliased with a camel case version of the column name
     */
    public SqlColumn<T> asCamelCase() {
        Builder<T> b = copy();
        return b.withAlias("\"" + StringUtilities.toCamelCase(name) + "\"").build(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public FragmentAndParameters renderForOrderBy(RenderingContext renderingContext) {
        return FragmentAndParameters.fromFragment(alias().orElse(name) + descendingPhrase);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        if (tableQualifier == null) {
            return FragmentAndParameters.fromFragment(renderingContext.aliasedColumnName(this));
        } else {
            return FragmentAndParameters.fromFragment(renderingContext.aliasedColumnName(this, tableQualifier));
        }
    }

    @Override
    public Optional<RenderingStrategy> renderingStrategy() {
        return Optional.ofNullable(renderingStrategy);
    }

    public <S> SqlColumn<S> withTypeHandler(String typeHandler) {
        Builder<S> b = copy();
        return b.withTypeHandler(typeHandler).build();
    }

    public <S> SqlColumn<S> withRenderingStrategy(RenderingStrategy renderingStrategy) {
        Builder<S> b = copy();
        return b.withRenderingStrategy(renderingStrategy).build();
    }

    public <S> SqlColumn<S> withParameterTypeConverter(ParameterTypeConverter<S, ?> parameterTypeConverter) {
        Builder<S> b = copy();
        return b.withParameterTypeConverter(parameterTypeConverter).build();
    }

    public <S> SqlColumn<S> withJavaType(Class<S> javaType) {
        Builder<S> b = copy();
        return b.withJavaType(javaType).build();
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
                .withDescendingPhrase(this.descendingPhrase)
                .withAlias(this.alias)
                .withTypeHandler(this.typeHandler)
                .withRenderingStrategy(this.renderingStrategy)
                .withParameterTypeConverter((ParameterTypeConverter<S, ?>) this.parameterTypeConverter)
                .withTableQualifier(this.tableQualifier)
                .withJavaType((Class<S>) this.javaType);
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
        protected @Nullable String name;
        protected @Nullable SqlTable table;
        protected @Nullable JDBCType jdbcType;
        protected String descendingPhrase = ""; //$NON-NLS-1$
        protected @Nullable String alias;
        protected @Nullable String typeHandler;
        protected @Nullable RenderingStrategy renderingStrategy;
        protected ParameterTypeConverter<T, ?> parameterTypeConverter = v -> v;
        protected @Nullable String tableQualifier;
        protected @Nullable Class<T> javaType;

        public Builder<T> withName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        public Builder<T> withJdbcType(@Nullable JDBCType jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }

        public Builder<T> withDescendingPhrase(String descendingPhrase) {
            this.descendingPhrase = descendingPhrase;
            return this;
        }

        public Builder<T> withAlias(@Nullable String alias) {
            this.alias = alias;
            return this;
        }

        public Builder<T> withTypeHandler(@Nullable String typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }

        public Builder<T> withRenderingStrategy(@Nullable RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder<T> withParameterTypeConverter(ParameterTypeConverter<T, ?> parameterTypeConverter) {
            this.parameterTypeConverter = parameterTypeConverter;
            return this;
        }

        private Builder<T> withTableQualifier(@Nullable String tableQualifier) {
            this.tableQualifier = tableQualifier;
            return this;
        }

        public Builder<T> withJavaType(@Nullable Class<T> javaType) {
            this.javaType = javaType;
            return this;
        }

        public SqlColumn<T> build() {
            return new SqlColumn<>(this);
        }
    }
}
