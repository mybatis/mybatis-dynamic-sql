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

import org.jetbrains.annotations.NotNull;

public class SqlTable implements TableExpression {

    protected String tableName;

    protected SqlTable(String tableName) {
        this.tableName = Objects.requireNonNull(tableName);
    }

    public String tableName() {
        return tableName;
    }

    public BasicColumn allColumns() {
        return SqlColumn.of("*", this); //$NON-NLS-1$
    }

    @NotNull
    public <T> SqlColumn<T> column(String name) {
        return SqlColumn.of(name, this);
    }

    @NotNull
    public <T> SqlColumn<T> column(String name, JDBCType jdbcType) {
        return SqlColumn.of(name, this, jdbcType);
    }

    @NotNull
    public <T> SqlColumn<T> column(String name, JDBCType jdbcType, String typeHandler) {
        SqlColumn<T> column = SqlColumn.of(name, this, jdbcType);
        return column.withTypeHandler(typeHandler);
    }

    @Override
    public <R> R accept(TableExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public Optional<String> tableAlias() {
        return Optional.empty();
    }

    public static SqlTable of(String name) {
        return new SqlTable(name);
    }
}
