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
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;

public class GeneralInsertDSL implements Buildable<GeneralInsertModel> {
    private final List<AbstractColumnMapping> columnMappings;
    private final SqlTable table;

    private GeneralInsertDSL(Builder builder) {
        table = Objects.requireNonNull(builder.table);
        columnMappings = builder.columnMappings;
    }

    public <T> SetClauseFinisher<T> set(SqlColumn<T> column) {
        return new SetClauseFinisher<>(column);
    }

    @NotNull
    @Override
    public GeneralInsertModel build() {
        return new GeneralInsertModel.Builder()
                .withTable(table)
                .withInsertMappings(columnMappings)
                .build();
    }

    public static GeneralInsertDSL insertInto(SqlTable table) {
        return new GeneralInsertDSL.Builder().withTable(table).build();
    }

    public class SetClauseFinisher<T> {

        private final SqlColumn<T> column;

        public SetClauseFinisher(SqlColumn<T> column) {
            this.column = column;
        }

        public GeneralInsertDSL toNull() {
            columnMappings.add(NullMapping.of(column));
            return GeneralInsertDSL.this;
        }

        public GeneralInsertDSL toConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return GeneralInsertDSL.this;
        }

        public GeneralInsertDSL toStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return GeneralInsertDSL.this;
        }

        public GeneralInsertDSL toValue(T value) {
            return toValue(() -> value);
        }

        public GeneralInsertDSL toValue(Supplier<T> valueSupplier) {
            columnMappings.add(ValueMapping.of(column, valueSupplier));
            return GeneralInsertDSL.this;
        }

        public GeneralInsertDSL toValueOrNull(T value) {
            return toValueOrNull(() -> value);
        }

        public GeneralInsertDSL toValueOrNull(Supplier<T> valueSupplier) {
            columnMappings.add(ValueOrNullMapping.of(column, valueSupplier));
            return GeneralInsertDSL.this;
        }

        public GeneralInsertDSL toValueWhenPresent(T value) {
            return toValueWhenPresent(() -> value);
        }

        public GeneralInsertDSL toValueWhenPresent(Supplier<T> valueSupplier) {
            columnMappings.add(ValueWhenPresentMapping.of(column, valueSupplier));
            return GeneralInsertDSL.this;
        }
    }

    public static class Builder {
        private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();
        private SqlTable table;

        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        public Builder withColumnMappings(Collection<AbstractColumnMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return this;
        }

        public GeneralInsertDSL build() {
            return new GeneralInsertDSL(this);
        }
    }
}
