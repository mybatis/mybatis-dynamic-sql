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
import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.NullCriterion;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.GroupByModel;
import org.mybatis.dynamic.sql.select.HavingApplier;
import org.mybatis.dynamic.sql.select.HavingModel;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;
import org.mybatis.dynamic.sql.where.WhereApplier;

public class SelectDSL implements
        JoinOperations<SelectDSL, SelectDSL.JoinSpecificationFinisher>,
        WhereOperations<SelectDSL.QueryExpressionWhereBuilder>,
        OrderByOperations<SelectDSL>,
        GroupByOperations<SelectDSL>,
        HavingOperations<SelectDSL.QueryExpressionHavingBuilder>,
        LimitAndOffsetOperations<SelectModel, SelectDSL>,
        ForAndWaitOperations<SelectDSL>,
        ConfigurableStatement<SelectDSL>,
        Buildable<SelectModel> {
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private CurrentQueryValues currentQueryValues = new CurrentQueryValues();
    private final List<QueryExpressionModel> unionQueries = new ArrayList<>();
    private @Nullable OrderByModel orderByModel;
    private @Nullable Long limit;
    private @Nullable Long offset;
    private @Nullable Long fetchFirstRows;
    private @Nullable String forClause;
    private @Nullable String waitClause;

    private static class CurrentQueryValues extends AbstractQueryingDSL {
        QueryExpressionModel.Builder builder = new QueryExpressionModel.Builder();
        @Nullable QueryExpressionWhereBuilder whereBuilder;
        @Nullable QueryExpressionHavingBuilder havingBuilder;

        QueryExpressionModel toQueryExpressionModel() {
            return builder
                    .withTableAliases(tableAliases())
                    .withTable(table())
                    .withJoinModel(buildJoinModel())
                    .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                    .withHavingModel(havingBuilder == null ? null : havingBuilder.buildHavingModel())
                    .build();
        }
    }

    private SelectDSL(Collection<? extends BasicColumn> selectList, boolean isDistinct) {
        currentQueryValues.builder.withSelectList(selectList);
        currentQueryValues.builder.isDistinct(isDistinct);
    }

    public static SelectDSL select(BasicColumn... selectList) {
        return select(Arrays.asList(selectList));
    }

    public static SelectDSL select(Collection<? extends BasicColumn> selectList) {
        return new SelectDSL(selectList, false);
    }

    public static SelectDSL selectDistinct(BasicColumn... selectList) {
        return selectDistinct(Arrays.asList(selectList));
    }

    public static SelectDSL selectDistinct(Collection<? extends BasicColumn> selectList) {
        return new SelectDSL(selectList, true);
    }

    public SelectDSL from(Buildable<SelectModel> select) {
        currentQueryValues.setTable(select);
        return this;
    }

    public SelectDSL from(Buildable<SelectModel> select, String tableAlias) {
        currentQueryValues.setTable(select, tableAlias);
        return this;
    }

    public SelectDSL from(SqlTable table) {
        currentQueryValues.setTable(table);
        return this;
    }

    public SelectDSL from(SqlTable table, String tableAlias) {
        currentQueryValues.setTable(table, tableAlias);
        return this;
    }

    @Override
    public JoinSpecificationFinisher join(JoinType joinType, TableExpression joinTable,
                                          SqlCriterion initialCriterion) {
        JoinSpecificationFinisher finisher = new JoinSpecificationFinisher(joinType, joinTable, initialCriterion);
        currentQueryValues.addJoinSpecification(finisher);
        return finisher;
    }

    @Override
    public SelectDSL endJoinSpecification() {
        return this;
    }

    @Override
    public void addTableAlias(SqlTable table, String tableAlias) {
        currentQueryValues.addTableAlias(table, tableAlias);
    }

    @Override
    public QueryExpressionWhereBuilder where() {
        if (currentQueryValues.whereBuilder == null) {
            currentQueryValues.whereBuilder = new QueryExpressionWhereBuilder(new NullCriterion());
        }
        return currentQueryValues.whereBuilder;
    }

    @Override
    public QueryExpressionWhereBuilder where(SqlCriterion initialCriterion) {
        Validator.assertNull(currentQueryValues.whereBuilder, Validator.ERROR_32);
        currentQueryValues.whereBuilder = new QueryExpressionWhereBuilder(initialCriterion);
        return currentQueryValues.whereBuilder;
    }

    @Override
    public QueryExpressionWhereBuilder applyWhere(WhereApplier whereApplier) {
        Validator.assertNull(currentQueryValues.whereBuilder, Validator.ERROR_32);
        currentQueryValues.whereBuilder =
                new QueryExpressionWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria());
        return currentQueryValues.whereBuilder;
    }

    @Override
    public SelectDSL orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return this;
    }

    public SelectDSL groupBy(Collection<? extends BasicColumn> columns) {
        currentQueryValues.builder.withGroupByModel(GroupByModel.of(columns));
        return this;
    }

    @Override
    public QueryExpressionHavingBuilder having(SqlCriterion initialCriterion) {
        Validator.assertNull(currentQueryValues.havingBuilder, "ERROR.31"); //$NON-NLS-1$
        currentQueryValues.havingBuilder = new QueryExpressionHavingBuilder(initialCriterion);
        return currentQueryValues.havingBuilder;
    }

    @Override
    public QueryExpressionHavingBuilder applyHaving(HavingApplier havingApplier) {
        Validator.assertNull(currentQueryValues.havingBuilder, "ERROR.31"); //$NON-NLS-1$
        currentQueryValues.havingBuilder =
                new QueryExpressionHavingBuilder(havingApplier.initialCriterion(), havingApplier.subCriteria());
        return currentQueryValues.havingBuilder;
    }

    @Override
    public LimitFinisher<SelectModel, SelectDSL> limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return new LimitFinisher<>(this);
    }

    @Override
    public OffsetFirstFinisher<SelectModel, SelectDSL> offsetWhenPresent(@Nullable Long offset) {
        this.offset = offset;
        return new OffsetFirstFinisher<>(this);
    }

    @Override
    public FetchFirstFinisher<SelectDSL> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return new FetchFirstFinisher<>(this);
    }

    private Optional<PagingModel> buildPagingModel() {
        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }

    @Override
    public SelectDSL setWaitClause(String waitClause) {
        Validator.assertNull(this.waitClause, "ERROR.49"); //$NON-NLS-1$
        this.waitClause = waitClause;
        return this;
    }

    @Override
    public SelectDSL setForClause(String forClause) {
        Validator.assertNull(this.forClause, "ERROR.48"); //$NON-NLS-1$
        this.forClause = forClause;
        return this;
    }

    public UnionBuilder union() {
        return new UnionBuilder("union"); //$NON-NLS-1$
    }

    public UnionBuilder unionAll() {
        return new UnionBuilder("union all"); //$NON-NLS-1$
    }

    @Override
    public SelectDSL configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    @Override
    public SelectModel build() {
        return new SelectModel.Builder()
                .withStatementConfiguration(statementConfiguration)
                .withQueryExpressions(unionQueries)
                .withQueryExpression(currentQueryValues.toQueryExpressionModel())
                .withOrderByModel(orderByModel)
                .withPagingModel(buildPagingModel().orElse(null))
                .withForClause(forClause)
                .withWaitClause(waitClause)
                .build();
    }

    public class QueryExpressionWhereBuilder implements BooleanOperations<QueryExpressionWhereBuilder>,
            ConfigurableStatement<QueryExpressionWhereBuilder>,
            OrderByOperations<SelectDSL>,
            GroupByOperations<SelectDSL>,
            LimitAndOffsetOperations<SelectModel, SelectDSL>,
            ForAndWaitOperations<SelectDSL>,
            Buildable<SelectModel> {
        private final SqlCriterion initialCriterion;
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        public QueryExpressionWhereBuilder(SqlCriterion initialCriterion) {
            this.initialCriterion = initialCriterion;
        }

        public QueryExpressionWhereBuilder(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
            this(initialCriterion);
            this.subCriteria.addAll(subCriteria);
        }

        @Override
        public QueryExpressionWhereBuilder addSubCriterion(AndOrCriteriaGroup subCriterion) {
            subCriteria.add(subCriterion);
            return this;
        }

        public UnionBuilder union() {
            return SelectDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return SelectDSL.this.unionAll();
        }

        @Override
        public LimitFinisher<SelectModel, SelectDSL> limitWhenPresent(@Nullable Long limit) {
            return SelectDSL.this.limitWhenPresent(limit);
        }

        @Override
        public OffsetFirstFinisher<SelectModel, SelectDSL> offsetWhenPresent(@Nullable Long offset) {
            return SelectDSL.this.offsetWhenPresent(offset);
        }

        @Override
        public FetchFirstFinisher<SelectDSL> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            return SelectDSL.this.fetchFirstWhenPresent(fetchFirstRows);
        }

        @Override
        public SelectDSL orderBy(Collection<? extends SortSpecification> columns) {
            return SelectDSL.this.orderBy(columns);
        }

        @Override
        public SelectDSL groupBy(Collection<? extends BasicColumn> columns) {
            return SelectDSL.this.groupBy(columns);
        }

        @Override
        public SelectModel build() {
            return SelectDSL.this.build();
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return new EmbeddedWhereModel.Builder()
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }

        @Override
        public QueryExpressionWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
            SelectDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public SelectDSL setWaitClause(String waitClause) {
            return SelectDSL.this.setWaitClause(waitClause);
        }

        @Override
        public SelectDSL setForClause(String forClause) {
            return SelectDSL.this.setForClause(forClause);
        }
    }

    public class JoinSpecificationFinisher
            extends AbstractJoinSpecificationFinisher<SelectDSL, JoinSpecificationFinisher>
            implements JoinOperations<SelectDSL, JoinSpecificationFinisher>,
            WhereOperations<QueryExpressionWhereBuilder>,
            ConfigurableStatement<JoinSpecificationFinisher>,
            GroupByOperations<SelectDSL>,
            OrderByOperations<SelectDSL>,
            LimitAndOffsetOperations<SelectModel, SelectDSL>,
            ForAndWaitOperations<SelectDSL>,
            Buildable<SelectModel> {

        protected JoinSpecificationFinisher(JoinType joinType, TableExpression joinTable,
                                            SqlCriterion initialCriterion) {
            super(joinType, joinTable, initialCriterion);
        }

        @Override
        public SelectModel build() {
            return SelectDSL.this.build();
        }

        @Override
        public JoinSpecificationFinisher configureStatement(Consumer<StatementConfiguration> consumer) {
            SelectDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public QueryExpressionWhereBuilder where() {
            return SelectDSL.this.where();
        }

        @Override
        public QueryExpressionWhereBuilder where(SqlCriterion initialCriterion) {
            return SelectDSL.this.where(initialCriterion);
        }

        @Override
        public QueryExpressionWhereBuilder applyWhere(WhereApplier whereApplier) {
            return SelectDSL.this.applyWhere(whereApplier);
        }

        @Override
        public SelectDSL groupBy(Collection<? extends BasicColumn> columns) {
            return SelectDSL.this.groupBy(columns);
        }

        public UnionBuilder union() {
            return SelectDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return SelectDSL.this.unionAll();
        }

        @Override
        public SelectDSL orderBy(Collection<? extends SortSpecification> columns) {
            return SelectDSL.this.orderBy(columns);
        }

        @Override
        protected JoinSpecificationFinisher getThis() {
            return this;
        }

        @Override
        public LimitFinisher<SelectModel, SelectDSL> limitWhenPresent(@Nullable Long limit) {
            return SelectDSL.this.limitWhenPresent(limit);
        }

        @Override
        public OffsetFirstFinisher<SelectModel, SelectDSL> offsetWhenPresent(@Nullable Long offset) {
            return SelectDSL.this.offsetWhenPresent(offset);
        }

        @Override
        public FetchFirstFinisher<SelectDSL> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            return SelectDSL.this.fetchFirstWhenPresent(fetchFirstRows);
        }

        @Override
        public SelectDSL setWaitClause(String waitClause) {
            return SelectDSL.this.setWaitClause(waitClause);
        }

        @Override
        public SelectDSL setForClause(String forClause) {
            return SelectDSL.this.setForClause(forClause);
        }

        @Override
        public JoinSpecificationFinisher join(JoinType joinType, TableExpression joinTable,
                                              SqlCriterion initialCriterion) {
            return SelectDSL.this.join(joinType, joinTable, initialCriterion);
        }

        @Override
        public SelectDSL endJoinSpecification() {
            return SelectDSL.this.endJoinSpecification();
        }

        @Override
        public void addTableAlias(SqlTable table, String tableAlias) {
            SelectDSL.this.addTableAlias(table, tableAlias);
        }
    }

    public class QueryExpressionHavingBuilder
            implements BooleanOperations<QueryExpressionHavingBuilder>,
            OrderByOperations<SelectDSL>,
            LimitAndOffsetOperations<SelectModel, SelectDSL>,
            ForAndWaitOperations<SelectDSL>,
            Buildable<SelectModel> {
        private final SqlCriterion initialCriterion;
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        public QueryExpressionHavingBuilder(SqlCriterion initialCriterion) {
            this.initialCriterion = initialCriterion;
        }

        public QueryExpressionHavingBuilder(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
            this(initialCriterion);
            this.subCriteria.addAll(subCriteria);
        }

        @Override
        public QueryExpressionHavingBuilder addSubCriterion(AndOrCriteriaGroup subCriterion) {
            subCriteria.add(subCriterion);
            return this;
        }

        @Override
        public SelectDSL orderBy(Collection<? extends SortSpecification> columns) {
            return SelectDSL.this.orderBy(columns);
        }

        public UnionBuilder union() {
            return SelectDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return SelectDSL.this.unionAll();
        }

        @Override
        public SelectModel build() {
            return SelectDSL.this.build();
        }

        protected HavingModel buildHavingModel() {
            return new HavingModel.Builder()
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }

        @Override
        public LimitFinisher<SelectModel, SelectDSL> limitWhenPresent(@Nullable Long limit) {
            return SelectDSL.this.limitWhenPresent(limit);
        }

        @Override
        public OffsetFirstFinisher<SelectModel, SelectDSL> offsetWhenPresent(@Nullable Long offset) {
            return SelectDSL.this.offsetWhenPresent(offset);
        }

        @Override
        public FetchFirstFinisher<SelectDSL> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            return SelectDSL.this.fetchFirstWhenPresent(fetchFirstRows);
        }

        @Override
        public SelectDSL setWaitClause(String waitClause) {
            return SelectDSL.this.setWaitClause(waitClause);
        }

        @Override
        public SelectDSL setForClause(String forClause) {
            return SelectDSL.this.setForClause(forClause);
        }
    }

    public class UnionBuilder {
        protected final String connector;

        public UnionBuilder(String connector) {
            this.connector = connector;
        }

        public SelectDSL select(BasicColumn... selectList) {
            return select(Arrays.asList(selectList));
        }

        public SelectDSL select(List<BasicColumn> selectList) {
            unionQueries.add(currentQueryValues.toQueryExpressionModel());
            currentQueryValues = new CurrentQueryValues();
            currentQueryValues.builder.withConnector(connector);
            currentQueryValues.builder.withSelectList(selectList);
            return SelectDSL.this;
        }

        public SelectDSL selectDistinct(BasicColumn... selectList) {
            return selectDistinct(Arrays.asList(selectList));
        }

        public SelectDSL selectDistinct(List<BasicColumn> selectList) {
            select(selectList);
            currentQueryValues.builder.isDistinct(true);
            return SelectDSL.this;
        }
    }
}
