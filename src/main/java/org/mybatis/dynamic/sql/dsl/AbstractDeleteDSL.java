/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public abstract class AbstractDeleteDSL<M, D extends AbstractDeleteDSL<M, D>>
        implements WhereOperations<AbstractDeleteDSL<M, D>.DeleteWhereBuilder>,
        ConfigurableStatement<D>,
        Buildable<M> {

    private final SqlTable table;
    private final @Nullable String tableAlias;
    private @Nullable DeleteWhereBuilder whereBuilder;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private @Nullable Long limit;
    private @Nullable OrderByModel orderByModel;

    protected AbstractDeleteDSL(SqlTable table, @Nullable String tableAlias) {
        this.table = Objects.requireNonNull(table);
        this.tableAlias = tableAlias;
    }

    @Override
    public DeleteWhereBuilder where() {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder, DeleteWhereBuilder::new);
        return whereBuilder;
    }

    public D limit(long limit) {
        return limitWhenPresent(limit);
    }

    public D limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return getThis();
    }

    public D orderBy(SortSpecification... columns) {
        return orderBy(Arrays.asList(columns));
    }

    public D orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return getThis();
    }

    protected abstract D getThis();

    /**
     * WARNING! Calling this method could result in a delete statement that deletes
     * all rows in a table.
     *
     * @return the model class
     */
    protected DeleteModel buildDeleteModel() {
        return DeleteModel.withTable(table)
                .withTableAlias(tableAlias)
                .withLimit(limit)
                .withOrderByModel(orderByModel)
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    @Override
    public D configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return getThis();
    }

    public class DeleteWhereBuilder extends AbstractBooleanOperationsFinisher<DeleteWhereBuilder>
            implements ConfigurableStatement<DeleteWhereBuilder>, Buildable<M> {

        public D limit(long limit) {
            return limitWhenPresent(limit);
        }

        public D limitWhenPresent(@Nullable Long limit) {
            return AbstractDeleteDSL.this.limitWhenPresent(limit);
        }

        public D orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public D orderBy(Collection<? extends SortSpecification> columns) {
            orderByModel = OrderByModel.of(columns);
            return AbstractDeleteDSL.this.getThis();
        }

        @Override
        public DeleteWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
            AbstractDeleteDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public M build() {
            return AbstractDeleteDSL.this.build();
        }

        @Override
        protected DeleteWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return toWhereModel();
        }
    }
}
