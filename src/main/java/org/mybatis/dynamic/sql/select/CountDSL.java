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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;
import org.mybatis.dynamic.sql.where.WhereModel;

/**
 * DSL for building count queries. Count queries are specializations of select queries. They have joins and where
 * clauses, but not the other parts of a select (group by, order by, etc.) Count queries always return
 * a long. If these restrictions are not acceptable, then use the Select DSL for an unrestricted select statement.
 *
 * @param <R> the type of model built by this Builder. Typically SelectModel.
 *
 * @author Jeff Butler
 */
public class CountDSL<R> extends AbstractQueryExpressionDSL<CountDSL<R>.CountWhereBuilder, CountDSL<R>>
        implements Buildable<R> {

    private final Function<SelectModel, R> adapterFunction;
    private CountWhereBuilder whereBuilder;
    private final BasicColumn countColumn;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();

    private CountDSL(BasicColumn countColumn, SqlTable table, Function<SelectModel, R> adapterFunction) {
        super(table);
        this.countColumn = Objects.requireNonNull(countColumn);
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    @Override
    public CountWhereBuilder where() {
        if (whereBuilder == null) {
            whereBuilder = new CountWhereBuilder();
        }
        return whereBuilder;
    }

    @NotNull
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
        QueryExpressionModel.Builder b = new QueryExpressionModel.Builder()
                .withSelectColumn(countColumn)
                .withTable(table())
                .withTableAliases(tableAliases());

        if (whereBuilder != null) {
            b.withWhereModel(whereBuilder.buildWhereModel());
        }

        buildJoinModel().ifPresent(b::withJoinModel);

        return new SelectModel.Builder()
                .withQueryExpression(b.build())
                .build();
    }

    public static CountDSL<SelectModel> countFrom(SqlTable table) {
        return countFrom(Function.identity(), table);
    }

    public static <R> CountDSL<R> countFrom(Function<SelectModel, R> adapterFunction, SqlTable table) {
        return new CountDSL<>(SqlBuilder.count(), table, adapterFunction);
    }

    public static FromGatherer<SelectModel> count(BasicColumn column) {
        return count(Function.identity(), column);
    }

    public static <R> FromGatherer<R> count(Function<SelectModel, R> adapterFunction, BasicColumn column) {
        return new FromGatherer<>(adapterFunction, SqlBuilder.count(column));
    }

    public static FromGatherer<SelectModel> countDistinct(BasicColumn column) {
        return countDistinct(Function.identity(), column);
    }

    public static <R> FromGatherer<R> countDistinct(Function<SelectModel, R> adapterFunction, BasicColumn column) {
        return new FromGatherer<>(adapterFunction, SqlBuilder.countDistinct(column));
    }

    @Override
    protected CountDSL<R> getThis() {
        return this;
    }

    public static class FromGatherer<R> {
        private final BasicColumn column;
        private final Function<SelectModel, R> adapterFunction;

        public FromGatherer(Function<SelectModel, R> adapterFunction, BasicColumn column) {
            this.adapterFunction = adapterFunction;
            this.column = column;
        }

        public CountDSL<R> from(SqlTable table) {
            return new CountDSL<>(column, table, adapterFunction);
        }
    }

    public class CountWhereBuilder extends AbstractWhereDSL<CountWhereBuilder>
            implements Buildable<R> {
        private CountWhereBuilder() {
            super(statementConfiguration);
        }

        @NotNull
        @Override
        public R build() {
            return CountDSL.this.build();
        }

        @Override
        protected CountWhereBuilder getThis() {
            return this;
        }

        protected WhereModel buildWhereModel() {
            return internalBuild();
        }
    }
}
