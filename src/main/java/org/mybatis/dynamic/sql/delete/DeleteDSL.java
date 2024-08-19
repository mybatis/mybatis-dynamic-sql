/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.delete;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.Utilities;
import org.mybatis.dynamic.sql.where.AbstractWhereFinisher;
import org.mybatis.dynamic.sql.where.AbstractWhereStarter;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class DeleteDSL<R> implements AbstractWhereStarter<DeleteDSL<R>.DeleteWhereBuilder, DeleteDSL<R>>,
        Buildable<R> {

    private final Function<DeleteModel, R> adapterFunction;
    private final SqlTable table;
    private final String tableAlias;
    private DeleteWhereBuilder whereBuilder;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private Long limit;
    private OrderByModel orderByModel;

    private DeleteDSL(SqlTable table, String tableAlias, Function<DeleteModel, R> adapterFunction) {
        this.table = Objects.requireNonNull(table);
        this.tableAlias = tableAlias;
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    @Override
    public DeleteWhereBuilder where() {
        whereBuilder = Utilities.buildIfNecessary(whereBuilder, DeleteWhereBuilder::new);
        return whereBuilder;
    }

    public DeleteDSL<R> limit(long limit) {
        return limitWhenPresent(limit);
    }

    public DeleteDSL<R> limitWhenPresent(Long limit) {
        this.limit = limit;
        return this;
    }

    public DeleteDSL<R> orderBy(SortSpecification... columns) {
        return orderBy(Arrays.asList(columns));
    }

    public DeleteDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return this;
    }

    /**
     * WARNING! Calling this method could result in a delete statement that deletes
     * all rows in a table.
     *
     * @return the model class
     */
    @NotNull
    @Override
    public R build() {
        DeleteModel deleteModel = DeleteModel.withTable(table)
                .withTableAlias(tableAlias)
                .withLimit(limit)
                .withOrderByModel(orderByModel)
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .withStatementConfiguration(statementConfiguration)
                .build();

        return adapterFunction.apply(deleteModel);
    }

    @Override
    public DeleteDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    public static <R> DeleteDSL<R> deleteFrom(Function<DeleteModel, R> adapterFunction, SqlTable table,
            String tableAlias) {
        return new DeleteDSL<>(table, tableAlias, adapterFunction);
    }

    public static DeleteDSL<DeleteModel> deleteFrom(SqlTable table) {
        return deleteFrom(Function.identity(), table, null);
    }

    public static DeleteDSL<DeleteModel> deleteFrom(SqlTable table, String tableAlias) {
        return deleteFrom(Function.identity(), table, tableAlias);
    }

    public class DeleteWhereBuilder extends AbstractWhereFinisher<DeleteWhereBuilder> implements Buildable<R> {

        private DeleteWhereBuilder() {
            super(DeleteDSL.this);
        }

        public DeleteDSL<R> limit(long limit) {
            return limitWhenPresent(limit);
        }

        public DeleteDSL<R> limitWhenPresent(Long limit) {
            return DeleteDSL.this.limitWhenPresent(limit);
        }

        public DeleteDSL<R> orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public DeleteDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            orderByModel = OrderByModel.of(columns);
            return DeleteDSL.this;
        }

        @NotNull
        @Override
        public R build() {
            return DeleteDSL.this.build();
        }

        @Override
        protected DeleteWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return buildModel();
        }
    }
}
