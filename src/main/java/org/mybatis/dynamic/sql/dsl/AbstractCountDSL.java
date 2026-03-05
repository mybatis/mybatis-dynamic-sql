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

import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

/**
 * DSL for building count queries. Count queries are specializations of select queries. They have joins and where
 * clauses, but not the other parts of a select (group by, order by, etc.) Count queries always return
 * a long value. If these restrictions are not acceptable, then use the Select DSL for an unrestricted select statement.
 *
 * @param <M> the type of model built by this Builder. Typically, SelectModel.
 * @param <D> the type of DSL builder
 *
 * @author Jeff Butler
 */
public abstract class AbstractCountDSL<M, D extends AbstractCountDSL<M, D>> extends AbstractQueryingDSL implements
        JoinOperations<D, AbstractCountDSL<M, D>.JoinSpecificationFinisher>,
        WhereOperations<AbstractCountDSL<M, D>.CountWhereBuilder>,
        ConfigurableStatement<D>, Buildable<M> {
    private @Nullable CountWhereBuilder whereBuilder;
    private final BasicColumn countColumn;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();

    protected AbstractCountDSL(BasicColumn column) {
        countColumn = Objects.requireNonNull(column);
    }

    public D from(SqlTable table) {
        setTable(table);
        return getThis();
    }

    public D from(SqlTable table, String tableAlias) {
        setTable(table, tableAlias);
        return getThis();
    }

    @Override
    public CountWhereBuilder where() {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder, CountWhereBuilder::new);
        return whereBuilder;
    }

    @Override
    public D configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return getThis();
    }

    protected SelectModel buildSelectModel() {
        QueryExpressionModel queryExpressionModel = new QueryExpressionModel.Builder()
                .withSelectColumn(countColumn)
                .withTable(table())
                .withTableAliases(tableAliases())
                .withJoinModel(buildJoinModel())
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .build();

        return new SelectModel.Builder()
                .withQueryExpression(queryExpressionModel)
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    protected abstract D getThis();

    @Override
    public JoinSpecificationFinisher buildJoinFinisher() {
        var finisher = new JoinSpecificationFinisher();
        addJoinSpecification(finisher);
        return finisher;
    }

    public class JoinSpecificationFinisher
            extends AbstractJoinSpecificationFinisher<D, JoinSpecificationFinisher>
            implements WhereOperations<CountWhereBuilder>,
            ConfigurableStatement<JoinSpecificationFinisher>, Buildable<M> {

        @Override
        protected JoinSpecificationFinisher getThis() {
            return this;
        }

        @Override
        public D endJoinSpecification() {
            return AbstractCountDSL.this.getThis();
        }

        @Override
        public CountWhereBuilder where() {
            return AbstractCountDSL.this.where();
        }

        @Override
        public M build() {
            return AbstractCountDSL.this.build();
        }

        @Override
        public JoinSpecificationFinisher configureStatement(Consumer<StatementConfiguration> consumer) {
            AbstractCountDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        protected void addTableAlias(SqlTable table, String tableAlias) {
            AbstractCountDSL.this.addTableAlias(table, tableAlias);
        }
    }

    public class CountWhereBuilder extends AbstractBooleanOperationsFinisher<CountWhereBuilder>
            implements ConfigurableStatement<CountWhereBuilder>, Buildable<M> {
        @Override
        public CountWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
            AbstractCountDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public M build() {
            return AbstractCountDSL.this.build();
        }

        @Override
        protected CountWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return toWhereModel();
        }
    }
}
