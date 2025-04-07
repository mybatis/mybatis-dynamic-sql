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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.OrderByModel;
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
import org.mybatis.dynamic.sql.where.AbstractWhereFinisher;
import org.mybatis.dynamic.sql.where.AbstractWhereStarter;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class UpdateDSL<R> implements AbstractWhereStarter<UpdateDSL<R>.UpdateWhereBuilder, UpdateDSL<R>>,
        Buildable<R> {

    private final Function<UpdateModel, R> adapterFunction;
    private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();
    private final SqlTable table;
    private final @Nullable String tableAlias;
    private @Nullable UpdateWhereBuilder whereBuilder;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private @Nullable Long limit;
    private @Nullable OrderByModel orderByModel;

    private UpdateDSL(SqlTable table, @Nullable String tableAlias, Function<UpdateModel, R> adapterFunction) {
        this.table = Objects.requireNonNull(table);
        this.tableAlias = tableAlias;
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    public <T> SetClauseFinisher<T> set(SqlColumn<T> column) {
        return new SetClauseFinisher<>(column);
    }

    @Override
    public UpdateWhereBuilder where() {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder, UpdateWhereBuilder::new);
        return whereBuilder;
    }

    public UpdateDSL<R> limit(long limit) {
        return limitWhenPresent(limit);
    }

    public UpdateDSL<R> limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return this;
    }

    public UpdateDSL<R> orderBy(SortSpecification... columns) {
        return orderBy(Arrays.asList(columns));
    }

    public UpdateDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return this;
    }

    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     *
     * @return the update model
     */
    @Override
    public R build() {
        UpdateModel updateModel = UpdateModel.withTable(table)
                .withTableAlias(tableAlias)
                .withColumnMappings(columnMappings)
                .withLimit(limit)
                .withOrderByModel(orderByModel)
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .withStatementConfiguration(statementConfiguration)
                .build();

        return adapterFunction.apply(updateModel);
    }

    @Override
    public UpdateDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    public static <R> UpdateDSL<R> update(Function<UpdateModel, R> adapterFunction, SqlTable table,
                                          @Nullable String tableAlias) {
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

        public UpdateDSL<R> equalToOrNull(@Nullable T value) {
            return equalToOrNull(() -> value);
        }

        public UpdateDSL<R> equalToOrNull(Supplier<@Nullable T> valueSupplier) {
            columnMappings.add(ValueOrNullMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalToWhenPresent(@Nullable T value) {
            return equalToWhenPresent(() -> value);
        }

        public UpdateDSL<R> equalToWhenPresent(Supplier<@Nullable T> valueSupplier) {
            columnMappings.add(ValueWhenPresentMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }
    }

    public class UpdateWhereBuilder extends AbstractWhereFinisher<UpdateWhereBuilder> implements Buildable<R> {

        private UpdateWhereBuilder() {
            super(UpdateDSL.this);
        }

        public UpdateDSL<R> limit(long limit) {
            return limitWhenPresent(limit);
        }

        public UpdateDSL<R> limitWhenPresent(@Nullable Long limit) {
            return UpdateDSL.this.limitWhenPresent(limit);
        }

        public UpdateDSL<R> orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public UpdateDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            orderByModel = OrderByModel.of(columns);
            return UpdateDSL.this;
        }

        @Override
        public R build() {
            return UpdateDSL.this.build();
        }

        @Override
        protected UpdateWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return buildModel();
        }
    }
}
