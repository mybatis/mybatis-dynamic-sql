/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.insert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.PropertyWhenPresentMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;

public class InsertDSL<T> implements Buildable<InsertModel<T>> {

    private final T row;
    private final SqlTable table;
    private final List<AbstractColumnMapping> columnMappings;

    private InsertDSL(Builder<T> builder) {
        this.row = Objects.requireNonNull(builder.row);
        this.table = Objects.requireNonNull(builder.table);
        columnMappings = builder.columnMappings;
    }

    public <F> ColumnMappingFinisher<F> map(SqlColumn<F> column) {
        return new ColumnMappingFinisher<>(column);
    }

    @NotNull
    @Override
    public InsertModel<T> build() {
        return InsertModel.withRow(row)
                .withTable(table)
                .withColumnMappings(columnMappings)
                .build();
    }

    public static <T> IntoGatherer<T> insert(T row) {
        return new IntoGatherer<>(row);
    }

    public static class IntoGatherer<T> {
        private final T row;

        private IntoGatherer(T row) {
            this.row = row;
        }

        public InsertDSL<T> into(SqlTable table) {
            return new InsertDSL.Builder<T>().withRow(row).withTable(table).build();
        }
    }

    public class ColumnMappingFinisher<F> {
        private final SqlColumn<F> column;

        public ColumnMappingFinisher(SqlColumn<F> column) {
            this.column = column;
        }

        public InsertDSL<T> toProperty(String property) {
            columnMappings.add(PropertyMapping.of(column, property));
            return InsertDSL.this;
        }

        public InsertDSL<T> toPropertyWhenPresent(String property, Supplier<?> valueSupplier) {
            columnMappings.add(PropertyWhenPresentMapping.of(column, property, valueSupplier));
            return InsertDSL.this;
        }

        public InsertDSL<T> toNull() {
            columnMappings.add(NullMapping.of(column));
            return InsertDSL.this;
        }

        public InsertDSL<T> toConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return InsertDSL.this;
        }

        public InsertDSL<T> toStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return InsertDSL.this;
        }
    }

    public static class Builder<T> {
        private T row;
        private SqlTable table;
        private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();

        public Builder<T> withRow(T row) {
            this.row = row;
            return this;
        }

        public Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        public Builder<T> withColumnMappings(Collection<AbstractColumnMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return this;
        }

        public InsertDSL<T> build() {
            return new InsertDSL<>(this);
        }
    }
}
