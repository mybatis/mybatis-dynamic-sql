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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.NullCriterion;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ColumnToColumnMapping;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.SelectMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;
import org.mybatis.dynamic.sql.where.WhereApplier;
import org.mybatis.dynamic.sql.where.WhereModel;

public abstract class AbstractUpdateDSL<M, D extends AbstractUpdateDSL<M, D>>
        implements WhereOperations<AbstractUpdateDSL<M, D>.UpdateWhereBuilder>,
        OrderByOperations<D>,
        ConfigurableStatement<D>,
        Buildable<M> {

    private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();
    private final SqlTable table;
    private final @Nullable String tableAlias;
    private @Nullable UpdateWhereBuilder whereBuilder;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private @Nullable Long limit;
    private @Nullable OrderByModel orderByModel;

    protected AbstractUpdateDSL(SqlTable table, @Nullable String tableAlias) {
        this.table = Objects.requireNonNull(table);
        this.tableAlias = tableAlias;
    }

    public <T> SetClauseFinisher<T> set(SqlColumn<T> column) {
        return new SetClauseFinisher<>(column);
    }

    @Override
    public UpdateWhereBuilder where() {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder, () -> new UpdateWhereBuilder(new NullCriterion()));
        return whereBuilder;
    }

    @Override
    public UpdateWhereBuilder where(SqlCriterion initialCriterion) {
        Validator.assertNull(whereBuilder, Validator.ERROR_32);
        whereBuilder = new UpdateWhereBuilder(initialCriterion);
        return whereBuilder;
    }

    @Override
    public UpdateWhereBuilder applyWhere(WhereApplier whereApplier) {
        Validator.assertNull(whereBuilder, Validator.ERROR_32);
        whereBuilder = new UpdateWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria());
        return whereBuilder;
    }

    public D limit(long limit) {
        return limitWhenPresent(limit);
    }

    public D limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return getThis();
    }

    @Override
    public D orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return getThis();
    }

    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     *
     * @return the update model
     */
    protected UpdateModel buildUpdateModel() {
        return UpdateModel.withTable(table)
                .withTableAlias(tableAlias)
                .withColumnMappings(columnMappings)
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

    protected abstract D getThis();

    public class SetClauseFinisher<T> {

        private final SqlColumn<T> column;

        public SetClauseFinisher(SqlColumn<T> column) {
            this.column = column;
        }

        public D equalToNull() {
            columnMappings.add(NullMapping.of(column));
            return AbstractUpdateDSL.this.getThis();
        }

        public D equalToConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return AbstractUpdateDSL.this.getThis();
        }

        public D equalToStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return AbstractUpdateDSL.this.getThis();
        }

        public D equalTo(T value) {
            return equalTo(() -> value);
        }

        public D equalTo(Supplier<T> valueSupplier) {
            columnMappings.add(ValueMapping.of(column, valueSupplier));
            return AbstractUpdateDSL.this.getThis();
        }

        public D equalTo(Buildable<SelectModel> buildable) {
            columnMappings.add(SelectMapping.of(column, buildable));
            return AbstractUpdateDSL.this.getThis();
        }

        public D equalTo(BasicColumn rightColumn) {
            columnMappings.add(ColumnToColumnMapping.of(column, rightColumn));
            return AbstractUpdateDSL.this.getThis();
        }

        public D equalToOrNull(@Nullable T value) {
            return equalToOrNull(() -> value);
        }

        public D equalToOrNull(Supplier<@Nullable T> valueSupplier) {
            columnMappings.add(ValueOrNullMapping.of(column, valueSupplier));
            return AbstractUpdateDSL.this.getThis();
        }

        public D equalToWhenPresent(@Nullable T value) {
            return equalToWhenPresent(() -> value);
        }

        public D equalToWhenPresent(Supplier<@Nullable T> valueSupplier) {
            columnMappings.add(ValueWhenPresentMapping.of(column, valueSupplier));
            return AbstractUpdateDSL.this.getThis();
        }
    }

    public class UpdateWhereBuilder
            implements BooleanOperations<UpdateWhereBuilder>, ConfigurableStatement<UpdateWhereBuilder>, Buildable<M> {
        private final SqlCriterion initialCriterion;
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        public UpdateWhereBuilder(SqlCriterion initialCriterion) {
            this.initialCriterion = initialCriterion;
        }

        public UpdateWhereBuilder(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
            this(initialCriterion);
            this.subCriteria.addAll(subCriteria);
        }

        @Override
        public UpdateWhereBuilder addSubCriterion(AndOrCriteriaGroup subCriterion) {
            subCriteria.add(subCriterion);
            return this;
        }

        public D limit(long limit) {
            return limitWhenPresent(limit);
        }

        public D limitWhenPresent(@Nullable Long limit) {
            return AbstractUpdateDSL.this.limitWhenPresent(limit);
        }

        public D orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public D orderBy(Collection<? extends SortSpecification> columns) {
            orderByModel = OrderByModel.of(columns);
            return AbstractUpdateDSL.this.getThis();
        }

        @Override
        public UpdateWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
            AbstractUpdateDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public M build() {
            return AbstractUpdateDSL.this.build();
        }

        protected WhereModel buildWhereModel() {
            return new WhereModel.Builder()
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }
    }
}
