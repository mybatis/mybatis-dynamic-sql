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

import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.StringUtilities;

/**
 * A derived column is a column that is not directly related to a table. This is primarily
 * used for supporting sub-queries. The main difference in this class and {@link SqlColumn} is
 * that this class does not have a related {@link SqlTable} and therefore ignores any table
 * alias set in a query. If a table alias is required it can be set directly in the
 * builder for this class.
 *
 * @param <T> The Java type that corresponds to this column - not used except for compiler type checking
 *           for conditions
 */
public class DerivedColumn<T> implements BindableColumn<T> {
    private final String name;
    private final String tableAlias;
    private final String columnAlias;
    private final JDBCType jdbcType;
    private final String typeHandler;

    protected DerivedColumn(Builder<T> builder) {
        this.name = Objects.requireNonNull(builder.name);
        this.tableAlias = builder.tableAlias;
        this.columnAlias = builder.columnAlias;
        this.jdbcType = builder.jdbcType;
        this.typeHandler = builder.typeHandler;
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(columnAlias);
    }

    @Override
    public Optional<JDBCType> jdbcType() {
        return Optional.ofNullable(jdbcType);
    }

    @Override
    public Optional<String> typeHandler() {
        return Optional.ofNullable(typeHandler);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return tableAlias == null ? name : tableAlias + "." + name; //$NON-NLS-1$
    }

    @Override
    public DerivedColumn<T> as(String columnAlias) {
        return new Builder<T>()
                .withName(name)
                .withColumnAlias(columnAlias)
                .withJdbcType(jdbcType)
                .withTypeHandler(typeHandler)
                .withTableAlias(tableAlias)
                .build();
    }

    public DerivedColumn<T> asCamelCase() {
        return as("\"" + StringUtilities.toCamelCase(name) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static <T> DerivedColumn<T> of(String name) {
        return new Builder<T>()
                .withName(name)
                .build();
    }

    public static <T> DerivedColumn<T> of(String name, String tableAlias) {
        return new Builder<T>()
                .withName(name)
                .withTableAlias(tableAlias)
                .build();
    }

    public static <T> DerivedColumn<T> from(SqlColumn<T> column) {
        return new Builder<T>()
                .withName(column.name())
                .withTypeHandler(column.typeHandler().orElse(null))
                .withJdbcType(column.jdbcType().orElse(null))
                .build();
    }

    public static <T> DerivedColumn<T> from(SqlColumn<T> column, String tableAlias) {
        return new Builder<T>()
                .withName(column.name())
                .withTableAlias(tableAlias)
                .withTypeHandler(column.typeHandler().orElse(null))
                .withJdbcType(column.jdbcType().orElse(null))
                .build();
    }

    public static class Builder<T> {
        private String name;
        private String tableAlias;
        private String columnAlias;
        private JDBCType jdbcType;
        private String typeHandler;

        public Builder<T> withName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> withTableAlias(String tableAlias) {
            this.tableAlias = tableAlias;
            return this;
        }

        public Builder<T> withColumnAlias(String columnAlias) {
            this.columnAlias = columnAlias;
            return this;
        }

        public Builder<T> withJdbcType(JDBCType jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }

        public Builder<T> withTypeHandler(String typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }

        public DerivedColumn<T> build() {
            return new DerivedColumn<>(this);
        }
    }
}
