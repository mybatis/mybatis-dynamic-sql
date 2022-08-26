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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ColumnToColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.SelectMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;
import org.mybatis.dynamic.sql.where.AbstractWhereSupport;
import org.mybatis.dynamic.sql.where.WhereModel;

public class UpdateDSL<R> extends AbstractWhereSupport<UpdateDSL<R>.UpdateWhereBuilder, UpdateDSL<R>>
        implements Buildable<R> {

    private final Function<UpdateModel, R> adapterFunction;
    private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();
    private final SqlTable table;
    private final String tableAlias;
    private UpdateWhereBuilder whereBuilder;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();

    private UpdateDSL(SqlTable table, String tableAlias, Function<UpdateModel, R> adapterFunction) {
        this.table = Objects.requireNonNull(table);
        this.tableAlias = tableAlias;
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    public <T> SetClauseFinisher<T> set(SqlColumn<T> column) {
        return new SetClauseFinisher<>(column);
    }

    @Override
    public UpdateWhereBuilder where() {
        if (whereBuilder == null) {
            whereBuilder = new UpdateWhereBuilder();
        }

        return whereBuilder;
    }

    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     *
     * @return the update model
     */
    @NotNull
    @Override
    public R build() {
        UpdateModel.Builder updateModelBuilder = UpdateModel.withTable(table)
                .withTableAlias(tableAlias)
                .withColumnMappings(columnMappings);

        if (whereBuilder != null) {
            updateModelBuilder.withWhereModel(whereBuilder.buildWhereModel());
        }

        return adapterFunction.apply(updateModelBuilder.build());
    }

    @Override
    public UpdateDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    public static <R> UpdateDSL<R> update(Function<UpdateModel, R> adapterFunction, SqlTable table, String tableAlias) {
        return new UpdateDSL<>(table, tableAlias, adapterFunction);
    }

    public static UpdateDSL<UpdateModel> update(SqlTable table) {
        return update(Function.identity(), table, null);
    }

    public static UpdateDSL<UpdateModel> update(SqlTable table, String tableAlias) {
        return update(Function.identity(), table, tableAlias);
    }

    public class SetClauseFinisher<T> {

        private final SqlColumn<T> column;

        public SetClauseFinisher(SqlColumn<T> column) {
            this.column = column;
        }

        public UpdateDSL<R> equalToNull() {
            columnMappings.add(NullMapping.of(column));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalToConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalToStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalTo(T value) {
            return equalTo(() -> value);
        }

        public UpdateDSL<R> equalTo(Supplier<T> valueSupplier) {
            columnMappings.add(ValueMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalTo(Buildable<SelectModel> buildable) {
            columnMappings.add(SelectMapping.of(column, buildable));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalTo(BasicColumn rightColumn) {
            columnMappings.add(ColumnToColumnMapping.of(column, rightColumn));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalToOrNull(T value) {
            return equalToOrNull(() -> value);
        }

        public UpdateDSL<R> equalToOrNull(Supplier<T> valueSupplier) {
            columnMappings.add(ValueOrNullMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalToWhenPresent(T value) {
            return equalToWhenPresent(() -> value);
        }

        public UpdateDSL<R> equalToWhenPresent(Supplier<T> valueSupplier) {
            columnMappings.add(ValueWhenPresentMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }
    }

    public class UpdateWhereBuilder extends AbstractWhereDSL<UpdateWhereBuilder> implements Buildable<R> {

        private UpdateWhereBuilder() {
            super(statementConfiguration);
        }

        @NotNull
        @Override
        public R build() {
            return UpdateDSL.this.build();
        }

        @Override
        protected UpdateWhereBuilder getThis() {
            return this;
        }

        protected WhereModel buildWhereModel() {
            return internalBuild();
        }
    }
}
