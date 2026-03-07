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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.NullCriterion;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;
import org.mybatis.dynamic.sql.where.WhereApplier;

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
        Validator.assertNull(whereBuilder, Validator.ERROR_32);
        whereBuilder = new CountWhereBuilder(new NullCriterion());
        return whereBuilder;
    }

    @Override
    public CountWhereBuilder where(SqlCriterion initialCriterion) {
        Validator.assertNull(whereBuilder, Validator.ERROR_32);
        whereBuilder = new CountWhereBuilder(initialCriterion);
        return whereBuilder;
    }

    @Override
    public CountWhereBuilder applyWhere(WhereApplier whereApplier) {
        Validator.assertNull(whereBuilder, Validator.ERROR_32);
        whereBuilder = new CountWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria());
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
    public JoinSpecificationFinisher join(JoinType joinType, TableExpression joinTable, SqlCriterion initialCriterion) {
        var finisher = new JoinSpecificationFinisher(joinType, joinTable, initialCriterion);
        addJoinSpecification(finisher);
        return finisher;
    }

    @Override
    public D endJoinSpecification() {
        return getThis();
    }

    public class JoinSpecificationFinisher
            extends AbstractJoinSpecificationFinisher<D, JoinSpecificationFinisher>
            implements WhereOperations<CountWhereBuilder>,
            ConfigurableStatement<JoinSpecificationFinisher>, Buildable<M> {

        protected JoinSpecificationFinisher(JoinType joinType, TableExpression joinTable,
                                            SqlCriterion initialCriterion) {
            super(joinType, joinTable, initialCriterion);
        }

        @Override
        protected JoinSpecificationFinisher getThis() {
            return this;
        }

        @Override
        public CountWhereBuilder where() {
            return AbstractCountDSL.this.where();
        }

        @Override
        public CountWhereBuilder where(SqlCriterion initialCriterion) {
            return AbstractCountDSL.this.where(initialCriterion);
        }

        @Override
        public CountWhereBuilder applyWhere(WhereApplier whereApplier) {
            return AbstractCountDSL.this.applyWhere(whereApplier);
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
    }

    public class CountWhereBuilder implements BooleanOperations<CountWhereBuilder>,
            ConfigurableStatement<CountWhereBuilder>, Buildable<M> {
        private final SqlCriterion initialCriterion;
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        public CountWhereBuilder(SqlCriterion initialCriterion) {
            this.initialCriterion = initialCriterion;
        }

        public CountWhereBuilder(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
            this(initialCriterion);
            this.subCriteria.addAll(subCriteria);
        }

        @Override
        public CountWhereBuilder addSubCriterion(AndOrCriteriaGroup subCriterion) {
            subCriteria.add(subCriterion);
            return this;
        }

        @Override
        public CountWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
            AbstractCountDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public M build() {
            return AbstractCountDSL.this.build();
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return new EmbeddedWhereModel.Builder()
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }
    }
}
