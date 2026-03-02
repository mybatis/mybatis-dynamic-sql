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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.dsl.AbstractBooleanOperationsFinisher;
import org.mybatis.dynamic.sql.dsl.AbstractJoinSpecificationFinisher;
import org.mybatis.dynamic.sql.dsl.AbstractQueryingDSL;
import org.mybatis.dynamic.sql.dsl.JoinOperations;
import org.mybatis.dynamic.sql.dsl.WhereOperations;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

/**
 * DSL for building count queries. Count queries are specializations of select queries. They have joins and where
 * clauses, but not the other parts of a select (group by, order by, etc.) Count queries always return
 * a long value. If these restrictions are not acceptable, then use the Select DSL for an unrestricted select statement.
 *
 * @param <R> the type of model built by this Builder. Typically, SelectModel.
 *
 * @author Jeff Butler
 */
public class CountDSL<R> extends AbstractQueryingDSL implements
        JoinOperations<CountDSL<R>, CountDSL<R>.JoinSpecificationFinisher>,
        WhereOperations<CountDSL<R>.CountWhereBuilder>,
        ConfigurableStatement<CountDSL<R>>,
        Buildable<R> {
    private final Function<SelectModel, R> adapterFunction;
    private @Nullable SqlTable table;
    private @Nullable CountWhereBuilder whereBuilder;
    private final BasicColumn countColumn;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private static final String ERROR_24 = "ERROR.24"; //$NON-NLS-1$

    private CountDSL(Builder<R> builder) {
        countColumn = Objects.requireNonNull(builder.column);
        adapterFunction = Objects.requireNonNull(builder.adapterFunction);
    }

    public CountDSL<R> from(SqlTable table) {
        Validator.assertNull(this.table, ERROR_24);
        this.table = table;
        return this;
    }

    public CountDSL<R> from(SqlTable table, String tableAlias) {
        Validator.assertNull(this.table, ERROR_24);
        addTableAlias(table, tableAlias);
        this.table = table;
        return this;
    }

    @Override
    public CountWhereBuilder where() {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder, CountWhereBuilder::new);
        return whereBuilder;
    }

    @Override
    public R build() {
        return adapterFunction.apply(buildModel());
    }

    @Override
    public CountDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    private SelectModel buildModel() {
        Validator.assertTrue(table != null, ERROR_24);
        QueryExpressionModel queryExpressionModel = new QueryExpressionModel.Builder()
                .withSelectColumn(countColumn)
                .withTable(table)
                .withTableAliases(tableAliases)
                .withJoinModel(buildJoinModel())
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .build();

        return new SelectModel.Builder()
                .withQueryExpression(queryExpressionModel)
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    public static CountDSL<SelectModel> countFrom(SqlTable table) {
        return countFrom(Function.identity(), table);
    }

    public static CountDSL<SelectModel> countFrom(SqlTable table, String tableAlias) {
        return new Builder<SelectModel>()
                .withAdapterFunction(Function.identity())
                .withColumn(SqlBuilder.count())
                .build()
                .from(table, tableAlias);
    }

    public static <R> CountDSL<R> countFrom(Function<SelectModel, R> adapterFunction, SqlTable table) {
        return new Builder<R>()
                .withAdapterFunction(adapterFunction)
                .withColumn(SqlBuilder.count())
                .build()
                .from(table);
    }

    public static CountDSL<SelectModel> count(BasicColumn column) {
        return count(Function.identity(), column);
    }

    public static <R> CountDSL<R> count(Function<SelectModel, R> adapterFunction, BasicColumn column) {
        return new Builder<R>()
                .withAdapterFunction(adapterFunction)
                .withColumn(SqlBuilder.count(column))
                .build();
    }

    public static CountDSL<SelectModel> countDistinct(BasicColumn column) {
        return countDistinct(Function.identity(), column);
    }

    public static <R> CountDSL<R> countDistinct(Function<SelectModel, R> adapterFunction, BasicColumn column) {
        return new Builder<R>()
                .withAdapterFunction(adapterFunction)
                .withColumn(SqlBuilder.countDistinct(column))
                .build();
    }

    @Override
    public JoinSpecificationFinisher buildJoinFinisher() {
        var finisher = new JoinSpecificationFinisher();
        joinSpecifications.add(finisher);
        return finisher;
    }

    public class JoinSpecificationFinisher
            extends AbstractJoinSpecificationFinisher<CountDSL<R>, JoinSpecificationFinisher>
            implements WhereOperations<CountWhereBuilder>,
            ConfigurableStatement<JoinSpecificationFinisher>, Buildable<R> {

        @Override
        protected JoinSpecificationFinisher getThis() {
            return this;
        }

        @Override
        public CountDSL<R> endJoinSpecification() {
            return CountDSL.this;
        }

        @Override
        public CountWhereBuilder where() {
            return CountDSL.this.where();
        }

        @Override
        public R build() {
            return CountDSL.this.build();
        }

        @Override
        public JoinSpecificationFinisher configureStatement(Consumer<StatementConfiguration> consumer) {
            CountDSL.this.configureStatement(consumer);
            return this;
        }
    }

    public class CountWhereBuilder extends AbstractBooleanOperationsFinisher<CountWhereBuilder>
            implements ConfigurableStatement<CountWhereBuilder>, Buildable<R> {
        @Override
        public CountWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
            CountDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public R build() {
            return CountDSL.this.build();
        }

        @Override
        protected CountWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return toWhereModel();
        }
    }

    public static class Builder<R> {
        private @Nullable BasicColumn column;
        private @Nullable Function<SelectModel, R> adapterFunction;

        public Builder<R> withColumn(BasicColumn column) {
            this.column = column;
            return this;
        }

        public Builder<R> withAdapterFunction(Function<SelectModel, R> adapterFunction) {
            this.adapterFunction = adapterFunction;
            return this;
        }

        public CountDSL<R> build() {
            return new CountDSL<>(this);
        }
    }
}
