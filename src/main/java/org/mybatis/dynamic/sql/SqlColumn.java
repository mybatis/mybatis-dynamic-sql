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

public class SqlColumn<T> implements BindableColumn<T>, SortSpecification, SqlColumnBuilders {

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
    protected final @Nullable String javaProperty;

    protected SqlColumn(AbstractBuilder<T, ?> builder) {
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
        javaProperty = builder.javaProperty;
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

    public Optional<String> javaProperty() {
        return Optional.ofNullable(javaProperty);
    }

    @Override
    public @Nullable Object convertParameterType(@Nullable T value) {
        return value == null ? null : parameterTypeConverter.convert(value);
    }

    @Override
    public SqlColumn<T> descending() {
        return copyBuilder().withDescendingPhrase(" DESC").build(); //$NON-NLS-1$
    }

    @Override
    public SqlColumn<T> as(String alias) {
        return copyBuilder().withAlias(alias).build();
    }

    /**
     * Override the calculated table qualifier if there is one. This is useful for sub-queries
     * where the calculated table qualifier may not be correct in all cases.
     *
     * @param tableQualifier the table qualifier to apply to the rendered column name
     * @return a new column that will be rendered with the specified table qualifier
     */
    public SqlColumn<T> qualifiedWith(String tableQualifier) {
        return copyBuilder().withTableQualifier(tableQualifier).build();
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
        return copyBuilder()
                .withAlias("\"" + StringUtilities.toCamelCase(name) + "\"").build(); //$NON-NLS-1$ //$NON-NLS-2$
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

    @Override
    public <S> SqlColumn<S> withTypeHandler(String typeHandler) {
        return cast(copyBuilder().withTypeHandler(typeHandler).build());
    }

    @Override
    public <S> SqlColumn<S> withRenderingStrategy(RenderingStrategy renderingStrategy) {
        return cast(copyBuilder().withRenderingStrategy(renderingStrategy).build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> SqlColumn<S> withParameterTypeConverter(ParameterTypeConverter<S, ?> parameterTypeConverter) {
        return cast(copyBuilder().withParameterTypeConverter((ParameterTypeConverter<T, ?>) parameterTypeConverter).build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> SqlColumn<S> withJavaType(Class<S> javaType) {
        return cast(copyBuilder().withJavaType((Class<T>) javaType).build());
    }

    @Override
    public <S> SqlColumn<S> withJavaProperty(String javaProperty) {
        return cast(copyBuilder().withJavaProperty(javaProperty).build());
    }

    private Builder<T> copyBuilder() {
        return populateBaseBuilder(new Builder<>());
    }

    @SuppressWarnings("unchecked")
    protected <S extends SqlColumn<?>> S cast(SqlColumn<?> column) {
        return (S) column;
    }

    /**
     * This method helps us tell a bit of fiction to the Java compiler. Java, for better or worse,
     * does not carry generic type information through chained methods. We want to enable method
     * chaining in the "with" methods. With this bit of fiction, we force the compiler to delay type
     * inference to the last method in the chain.
     *
     * @param <B> the concrete builder type
     * @return the populated builder
     */
    @SuppressWarnings("unchecked")
    protected <B extends AbstractBuilder<T, ?>> B populateBaseBuilder(B builder) {
        return (B) builder
                .withName(this.name)
                .withTable(this.table)
                .withJdbcType(this.jdbcType)
                .withDescendingPhrase(this.descendingPhrase)
                .withAlias(this.alias)
                .withTypeHandler(this.typeHandler)
                .withRenderingStrategy(this.renderingStrategy)
                .withParameterTypeConverter(this.parameterTypeConverter)
                .withTableQualifier(this.tableQualifier)
                .withJavaType(this.javaType)
                .withJavaProperty(this.javaProperty);
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

    public static abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>> {
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
        protected @Nullable String javaProperty;

        public B withName(String name) {
            this.name = name;
            return getThis();
        }

        public B withTable(SqlTable table) {
            this.table = table;
            return getThis();
        }

        public B withJdbcType(@Nullable JDBCType jdbcType) {
            this.jdbcType = jdbcType;
            return getThis();
        }

        public B withDescendingPhrase(String descendingPhrase) {
            this.descendingPhrase = descendingPhrase;
            return getThis();
        }

        public B withAlias(@Nullable String alias) {
            this.alias = alias;
            return getThis();
        }

        public B withTypeHandler(@Nullable String typeHandler) {
            this.typeHandler = typeHandler;
            return getThis();
        }

        public B withRenderingStrategy(@Nullable RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return getThis();
        }

        public B withParameterTypeConverter(ParameterTypeConverter<T, ?> parameterTypeConverter) {
            this.parameterTypeConverter = parameterTypeConverter;
            return getThis();
        }

        public B withTableQualifier(@Nullable String tableQualifier) {
            this.tableQualifier = tableQualifier;
            return getThis();
        }

        public B withJavaType(@Nullable Class<T> javaType) {
            this.javaType = javaType;
            return getThis();
        }

        public B withJavaProperty(@Nullable String javaProperty) {
            this.javaProperty = javaProperty;
            return getThis();
        }

        protected abstract B getThis();
    }

    public static class Builder<T> extends AbstractBuilder<T, Builder<T>> {
        public SqlColumn<T> build() {
            return new SqlColumn<>(this);
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }
    }
}
