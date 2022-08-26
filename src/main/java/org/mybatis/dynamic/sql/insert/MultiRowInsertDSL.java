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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;

public class MultiRowInsertDSL<T> implements Buildable<MultiRowInsertModel<T>> {

    private final Collection<T> records;
    private final SqlTable table;
    private final List<AbstractColumnMapping> columnMappings;

    private MultiRowInsertDSL(BatchInsertDSL.AbstractBuilder<T, ?> builder) {
        this.records = builder.records;
        this.table = Objects.requireNonNull(builder.table);
        this.columnMappings = builder.columnMappings;
    }

    public <F> ColumnMappingFinisher<F> map(SqlColumn<F> column) {
        return new ColumnMappingFinisher<>(column);
    }

    @NotNull
    @Override
    public MultiRowInsertModel<T> build() {
        return MultiRowInsertModel.withRecords(records)
                .withTable(table)
                .withColumnMappings(columnMappings)
                .build();
    }

    @SafeVarargs
    public static <T> IntoGatherer<T> insert(T... records) {
        return MultiRowInsertDSL.insert(Arrays.asList(records));
    }

    public static <T> IntoGatherer<T> insert(Collection<T> records) {
        return new IntoGatherer<>(records);
    }

    public static class IntoGatherer<T> {
        private final Collection<T> records;

        private IntoGatherer(Collection<T> records) {
            this.records = records;
        }

        public MultiRowInsertDSL<T> into(SqlTable table) {
            return new Builder<T>().withRecords(records).withTable(table).build();
        }
    }

    public class ColumnMappingFinisher<F> {
        private final SqlColumn<F> column;

        public ColumnMappingFinisher(SqlColumn<F> column) {
            this.column = column;
        }

        public MultiRowInsertDSL<T> toProperty(String property) {
            columnMappings.add(PropertyMapping.of(column, property));
            return MultiRowInsertDSL.this;
        }

        public MultiRowInsertDSL<T> toNull() {
            columnMappings.add(NullMapping.of(column));
            return MultiRowInsertDSL.this;
        }

        public MultiRowInsertDSL<T> toConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return MultiRowInsertDSL.this;
        }

        public MultiRowInsertDSL<T> toStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return MultiRowInsertDSL.this;
        }
    }

    public static class Builder<T> extends BatchInsertDSL.AbstractBuilder<T, Builder<T>> {

        @Override
        protected Builder<T> getThis() {
            return this;
        }

        public MultiRowInsertDSL<T> build() {
            return new MultiRowInsertDSL<>(this);
        }
    }
}
