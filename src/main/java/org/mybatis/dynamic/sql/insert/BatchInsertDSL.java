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

public class BatchInsertDSL<T> implements Buildable<BatchInsertModel<T>> {

    private final Collection<T> records;
    private final SqlTable table;
    private final List<AbstractColumnMapping> columnMappings;

    private BatchInsertDSL(AbstractBuilder<T, ?> builder) {
        this.records = builder.records;
        this.table = Objects.requireNonNull(builder.table);
        this.columnMappings = builder.columnMappings;
    }

    public <F> ColumnMappingFinisher<F> map(SqlColumn<F> column) {
        return new ColumnMappingFinisher<>(column);
    }

    @NotNull
    @Override
    public BatchInsertModel<T> build() {
        return BatchInsertModel.withRecords(records)
                .withTable(table)
                .withColumnMappings(columnMappings)
                .build();
    }

    @SafeVarargs
    public static <T> IntoGatherer<T> insert(T... records) {
        return BatchInsertDSL.insert(Arrays.asList(records));
    }

    public static <T> IntoGatherer<T> insert(Collection<T> records) {
        return new IntoGatherer<>(records);
    }

    public static class IntoGatherer<T> {
        private final Collection<T> records;

        private IntoGatherer(Collection<T> records) {
            this.records = records;
        }

        public BatchInsertDSL<T> into(SqlTable table) {
            return new Builder<T>().withRecords(records).withTable(table).build();
        }
    }

    public class ColumnMappingFinisher<F> {
        private final SqlColumn<F> column;

        public ColumnMappingFinisher(SqlColumn<F> column) {
            this.column = column;
        }

        public BatchInsertDSL<T> toProperty(String property) {
            columnMappings.add(PropertyMapping.of(column, property));
            return BatchInsertDSL.this;
        }

        public BatchInsertDSL<T> toNull() {
            columnMappings.add(NullMapping.of(column));
            return BatchInsertDSL.this;
        }

        public BatchInsertDSL<T> toConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return BatchInsertDSL.this;
        }

        public BatchInsertDSL<T> toStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return BatchInsertDSL.this;
        }
    }

    public abstract static class AbstractBuilder<T, B extends AbstractBuilder<T, B>> {
        final Collection<T> records = new ArrayList<>();
        SqlTable table;
        final List<AbstractColumnMapping> columnMappings = new ArrayList<>();

        public B withRecords(Collection<T> records) {
            this.records.addAll(records);
            return getThis();
        }

        public B withTable(SqlTable table) {
            this.table = table;
            return getThis();
        }

        public B withColumnMappings(Collection<AbstractColumnMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return getThis();
        }

        protected abstract B getThis();
    }

    public static class Builder<T> extends AbstractBuilder<T, Builder<T>> {
        @Override
        protected Builder<T> getThis() {
            return this;
        }

        public BatchInsertDSL<T> build() {
            return new BatchInsertDSL<>(this);
        }
    }
}
