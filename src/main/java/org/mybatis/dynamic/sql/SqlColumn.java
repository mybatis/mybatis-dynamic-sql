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

/**
 * This class represents the definition of a column in a table.
 *
 * <p>The class contains many attributes that are helpful for use in MyBatis and Spring runtime
 * environments, but the only required attributes are the name of the column and a reference to
 * the {@link SqlTable} the column is a part of.
 *
 * <p>The class can be extended if you wish to associate additional attributes with a column for your
 * own purposes. Extending the class is a bit more challenging than you might expect because you will need to
 * handle the covariant types for many methods in {@code SqlColumn}. Additionally, many methods in {@code SqlColumn}
 * create new instances of the class in keeping with the library's primary strategy of immutability. You will also
 * need to ensure that these methods create instances of your extended class, rather than the base {@code SqlColumn}
 * class. We have worked to keep this process as simple as possible.
 *
 * <p>Extending the class involves the following activities:
 * <ol>
 *     <li>Create a class that extends {@link SqlColumn}</li>
 *     <li>In your extended class, create a static builder class that extends {@link SqlColumn.AbstractBuilder}</li>
 *     <li>Add your desired attributes to the class and the builder</li>
 *     <li>In your extended class, override the {@link SqlColumn#copyBuilder()} method and return a new instance of
 *       your builder with all attributes set. You should call the
 *       {@link SqlColumn#populateBaseBuilder(AbstractBuilder)} method
 *       to set the attributes from {@code SqlColumn}, then populate your extended attributes.
 *     </li>
 *     <li>You MUST override the following methods. These methods are used with regular operations in the library.
 *         If you do not override these methods, it is likely that your extended attributes will be lost during
 *         regular usage. For example, if you do not override the {@code as} method and a user calls the method to
 *         apply an alias, then the base {@code SqlColumn} class would create a new instance of {@code SqlColumn}, NOT
 *         your extended class.
 *       <ul>
 *           <li>{@link SqlColumn#as(String)}</li>
 *           <li>{@link SqlColumn#asCamelCase()}</li>
 *           <li>{@link SqlColumn#descending()}</li>
 *           <li>{@link SqlColumn#qualifiedWith(String)}</li>
 *       </ul>
 *     </li>
 *     <li>You SHOULD override the following methods. These methods can be used to add additional attributes to a
 *         column by creating a new instance with a specified attribute set. These methods are used during the
 *         construction of columns. If you do not override these methods, and a user calls them, then a new
 *         {@code SqlColumn} will be created that does not contain your extended attributes.
 *       <ul>
 *           <li>{@link SqlColumn#withJavaProperty(String)}</li>
 *           <li>{@link SqlColumn#withRenderingStrategy(RenderingStrategy)}</li>
 *           <li>{@link SqlColumn#withTypeHandler(String)}</li>
 *           <li>{@link SqlColumn#withJavaType(Class)}</li>
 *           <li>{@link SqlColumn#withParameterTypeConverter(ParameterTypeConverter)}</li>
 *       </ul>
 *     </li>
 * </ol>
 *
 * <p>For all overridden methods except {@code copyBuilder()}, the process is to call the superclass
 * method and cast the result properly. We provide a {@link SqlColumn#cast(SqlColumn)} method to aid with this
 * process. For example, overriding the {@code descending} method could look like this:
 *
 * <pre>
 * {@code
 * @Override
 * public MyExtendedColumn<T> descending() {
 *     return cast(super.descending());
 * }
 * }
 * </pre>
 *
 * <p>The test code for this library contains an example of a proper extension of this class.
 *
 * @param <T> the Java type associated with the column
 */
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
    protected final @Nullable String javaProperty;

    protected SqlColumn(AbstractBuilder<T, ?, ?> builder) {
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

    /**
     * Create a new column instance that will render as descending when used in an order by phrase.
     *
     * @return a new column instance that will render as descending when used in an order by phrase
     */
    @Override
    public SqlColumn<T> descending() {
        return cast(copyBuilder().withDescendingPhrase(" DESC").build()); //$NON-NLS-1$
    }

    /**
     * Create a new column instance with the specified alias that will render as "as alias" in a column list.
     *
     * @param alias
     *            the column alias to set
     *
     * @return a new column instance with the specified alias
     */
    @Override
    public SqlColumn<T> as(String alias) {
        return cast(copyBuilder().withAlias(alias).build());
    }

    /**
     * Override the calculated table qualifier if there is one. This is useful for sub-queries
     * where the calculated table qualifier may not be correct in all cases.
     *
     * @param tableQualifier the table qualifier to apply to the rendered column name
     * @return a new column that will be rendered with the specified table qualifier
     */
    public SqlColumn<T> qualifiedWith(String tableQualifier) {
        return cast(copyBuilder().withTableQualifier(tableQualifier).build());
    }

    /**
     * Set an alias with a camel-cased string based on the column name. This can be useful for queries using
     * the {@link org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper} where the columns are placed into
     * a map based on the column name returned from the database.
     *
     * <p>A camel case string is a mixed case string, and most databases do not support unquoted mixed case strings
     * as identifiers. Therefore, the generated alias will be surrounded by double quotes thereby making it a
     * quoted identifier. Most databases will respect quoted mixed case identifiers.
     *
     * @return a new column aliased with a camel case version of the column name
     */
    public SqlColumn<T> asCamelCase() {
        return cast(copyBuilder()
                .withAlias("\"" + StringUtilities.toCamelCase(name) + "\"") //$NON-NLS-1$ //$NON-NLS-2$
                .build());
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

    /**
     * Create a new column instance with the specified type handler.
     *
     * <p>This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param typeHandler the type handler to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
     */
    public <S> SqlColumn<S> withTypeHandler(String typeHandler) {
        return cast(copyBuilder().withTypeHandler(typeHandler).build());
    }

    /**
     * Create a new column instance with the specified rendering strategy.
     *
     * <p>This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param renderingStrategy the rendering strategy to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
     */
    public <S> SqlColumn<S> withRenderingStrategy(RenderingStrategy renderingStrategy) {
        return cast(copyBuilder().withRenderingStrategy(renderingStrategy).build());
    }

    /**
     * Create a new column instance with the specified parameter type converter.
     *
     * <p>Parameter type converters are useful with Spring JDBC. Typically, they are not needed for MyBatis.
     *
     * <p>This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param parameterTypeConverter the parameter type converter to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
     */
    @SuppressWarnings("unchecked")
    public <S> SqlColumn<S> withParameterTypeConverter(ParameterTypeConverter<S, ?> parameterTypeConverter) {
        return cast(copyBuilder().withParameterTypeConverter((ParameterTypeConverter<T, ?>) parameterTypeConverter)
                .build());
    }

    /**
     * Create a new column instance with the specified Java type.
     *
     * <p>Specifying a Java type will force rendering of the Java type for MyBatis parameters. This can be useful
     * with some MyBatis type handlers.
     *
     * <p>This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param javaType the Java type to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
     */
    @SuppressWarnings("unchecked")
    public <S> SqlColumn<S> withJavaType(Class<S> javaType) {
        return cast(copyBuilder().withJavaType((Class<T>) javaType).build());
    }

    /**
     * Create a new column instance with the specified Java property.
     *
     * <p>Specifying a Java property in the column will allow usage of the column as a "mapped column" in record-based
     * insert statements.
     *
     * <p>This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param javaProperty the Java property to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
     */
    public <S> SqlColumn<S> withJavaProperty(String javaProperty) {
        return cast(copyBuilder().withJavaProperty(javaProperty).build());
    }

    protected AbstractBuilder<T, ?, ?> copyBuilder() {
        return populateBaseBuilder(new Builder<>());
    }

    @SuppressWarnings("unchecked")
    protected <S extends SqlColumn<?>> S cast(SqlColumn<?> column) {
        return (S) column;
    }

    /**
     * This method will add all current attributes to the specified builder. It is useful when creating
     * new class instances that only change one attribute - we set all current attributes, then
     * change the one attribute. This utility can be used with the with* methods and other methods that
     * create new instances.
     *
     * @param <B> the concrete builder type
     * @return the populated builder
     */
    @SuppressWarnings("unchecked")
    protected <B extends AbstractBuilder<T, ?, ?>> B populateBaseBuilder(B builder) {
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

    public abstract static class AbstractBuilder<T, C extends SqlColumn<T>, B extends AbstractBuilder<T, C, B>> {
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

        public abstract C build();
    }

    public static class Builder<T> extends AbstractBuilder<T, SqlColumn<T>, Builder<T>> {
        @Override
        public SqlColumn<T> build() {
            return new SqlColumn<>(this);
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }
    }
}
