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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.Utilities;
import org.mybatis.dynamic.sql.where.AbstractWhereFinisher;
import org.mybatis.dynamic.sql.where.AbstractWhereStarter;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class QueryExpressionDSL<R>
        extends AbstractQueryExpressionDSL<QueryExpressionDSL<R>.QueryExpressionWhereBuilder, QueryExpressionDSL<R>>
        implements Buildable<R> {

    private final String connector;
    private final SelectDSL<R> selectDSL;
    private final boolean isDistinct;
    private final List<BasicColumn> selectList;
    private QueryExpressionWhereBuilder whereBuilder;
    private GroupByModel groupByModel;
    private QueryExpressionHavingBuilder havingBuilder;

    protected QueryExpressionDSL(FromGatherer<R> fromGatherer, TableExpression table) {
        super(table);
        connector = fromGatherer.connector;
        selectList = fromGatherer.selectList;
        isDistinct = fromGatherer.isDistinct;
        selectDSL = Objects.requireNonNull(fromGatherer.selectDSL);
        selectDSL.registerQueryExpression(this);
    }

    protected QueryExpressionDSL(FromGatherer<R> fromGatherer, SqlTable table, String tableAlias) {
        this(fromGatherer, table);
        addTableAlias(table, tableAlias);
    }

    @Override
    public QueryExpressionWhereBuilder where() {
        whereBuilder = Utilities.buildIfNecessary(whereBuilder, QueryExpressionWhereBuilder::new);
        return whereBuilder;
    }

    @Override
    public QueryExpressionDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        selectDSL.configureStatement(consumer);
        return this;
    }

    /**
     * This method is protected here because it doesn't make sense at this point in the DSL.
     *
     * @return The having builder
     */
    protected QueryExpressionHavingBuilder having() {
        havingBuilder = Utilities.buildIfNecessary(havingBuilder, QueryExpressionHavingBuilder::new);
        return havingBuilder;
    }

    /**
     * This method is meant for use by the Kotlin DSL. We expect a full set of criteria.
     *
     * @param criteriaGroup the full criteria for a Kotlin Having clause
     */
    protected void applyHaving(CriteriaGroup criteriaGroup) {
        having().initialize(criteriaGroup);
    }

    @NotNull
    @Override
    public R build() {
        return selectDSL.build();
    }

    public JoinSpecificationStarter join(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.INNER);
    }

    public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable);
    }

    public JoinSpecificationStarter join(Buildable<SelectModel> joinTable, String tableAlias) {
        return new JoinSpecificationStarter(buildSubQuery(joinTable, tableAlias), JoinType.INNER);
    }

    public JoinSpecificationStarter leftJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.LEFT);
    }

    public JoinSpecificationStarter leftJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable);
    }

    public JoinSpecificationStarter leftJoin(Buildable<SelectModel> joinTable, String tableAlias) {
        return new JoinSpecificationStarter(buildSubQuery(joinTable, tableAlias), JoinType.LEFT);
    }

    public JoinSpecificationStarter rightJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.RIGHT);
    }

    public JoinSpecificationStarter rightJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable);
    }

    public JoinSpecificationStarter rightJoin(Buildable<SelectModel> joinTable, String tableAlias) {
        return new JoinSpecificationStarter(buildSubQuery(joinTable, tableAlias), JoinType.RIGHT);
    }

    public JoinSpecificationStarter fullJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.FULL);
    }

    public JoinSpecificationStarter fullJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable);
    }

    public JoinSpecificationStarter fullJoin(Buildable<SelectModel> joinTable, String tableAlias) {
        return new JoinSpecificationStarter(buildSubQuery(joinTable, tableAlias), JoinType.FULL);
    }

    public GroupByFinisher groupBy(BasicColumn... columns) {
        return groupBy(Arrays.asList(columns));
    }

    public GroupByFinisher groupBy(Collection<? extends BasicColumn> columns) {
        groupByModel = GroupByModel.of(columns);
        return new GroupByFinisher();
    }

    public SelectDSL<R> orderBy(SortSpecification... columns) {
        return orderBy(Arrays.asList(columns));
    }

    public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
        selectDSL.orderBy(columns);
        return selectDSL;
    }

    public UnionBuilder union() {
        return new UnionBuilder("union"); //$NON-NLS-1$
    }

    public UnionBuilder unionAll() {
        return new UnionBuilder("union all"); //$NON-NLS-1$
    }

    protected QueryExpressionModel buildModel() {
        return QueryExpressionModel.withSelectList(selectList)
                .withConnector(connector)
                .withTable(table())
                .isDistinct(isDistinct)
                .withTableAliases(tableAliases())
                .withJoinModel(buildJoinModel().orElse(null))
                .withGroupByModel(groupByModel)
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .withHavingModel(havingBuilder == null ? null : havingBuilder.buildHavingModel())
                .build();
    }

    public SelectDSL<R>.LimitFinisher limit(long limit) {
        return selectDSL.limit(limit);
    }

    public SelectDSL<R>.OffsetFirstFinisher offset(long offset) {
        return selectDSL.offset(offset);
    }

    public SelectDSL<R>.FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        return selectDSL.fetchFirst(fetchFirstRows);
    }

    @Override
    protected QueryExpressionDSL<R> getThis() {
        return this;
    }

    public static class FromGatherer<R> {
        private final String connector;
        private final List<BasicColumn> selectList;
        private final SelectDSL<R> selectDSL;
        private final boolean isDistinct;

        public FromGatherer(Builder<R> builder) {
            this.connector = builder.connector;
            this.selectList = builder.selectList;
            this.selectDSL = Objects.requireNonNull(builder.selectDSL);
            this.isDistinct = builder.isDistinct;
        }

        public QueryExpressionDSL<R> from(Buildable<SelectModel> select) {
            return new QueryExpressionDSL<>(this, buildSubQuery(select));
        }

        public QueryExpressionDSL<R> from(Buildable<SelectModel> select, String tableAlias) {
            return new QueryExpressionDSL<>(this, buildSubQuery(select, tableAlias));
        }

        public QueryExpressionDSL<R> from(SqlTable table) {
            return new QueryExpressionDSL<>(this, table);
        }

        public QueryExpressionDSL<R> from(SqlTable table, String tableAlias) {
            return new QueryExpressionDSL<>(this, table, tableAlias);
        }

        public static class Builder<R> {
            private String connector;
            private final List<BasicColumn> selectList = new ArrayList<>();
            private SelectDSL<R> selectDSL;
            private boolean isDistinct;

            public Builder<R> withConnector(String connector) {
                this.connector = connector;
                return this;
            }

            public Builder<R> withSelectList(Collection<? extends BasicColumn> selectList) {
                this.selectList.addAll(selectList);
                return this;
            }

            public Builder<R> withSelectDSL(SelectDSL<R> selectDSL) {
                this.selectDSL = selectDSL;
                return this;
            }

            public Builder<R> isDistinct() {
                this.isDistinct = true;
                return this;
            }

            public FromGatherer<R> build() {
                return new FromGatherer<>(this);
            }
        }
    }

    public class QueryExpressionWhereBuilder extends AbstractWhereFinisher<QueryExpressionWhereBuilder>
            implements Buildable<R> {
        private QueryExpressionWhereBuilder() {
            super(QueryExpressionDSL.this);
        }

        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        public SelectDSL<R> orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

        public GroupByFinisher groupBy(BasicColumn... columns) {
            return groupBy(Arrays.asList(columns));
        }

        public GroupByFinisher groupBy(Collection<? extends BasicColumn> columns) {
            return QueryExpressionDSL.this.groupBy(columns);
        }

        public SelectDSL<R>.LimitFinisher limit(long limit) {
            return QueryExpressionDSL.this.limit(limit);
        }

        public SelectDSL<R>.OffsetFirstFinisher offset(long offset) {
            return QueryExpressionDSL.this.offset(offset);
        }

        public SelectDSL<R>.FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            return QueryExpressionDSL.this.fetchFirst(fetchFirstRows);
        }

        @NotNull
        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }

        @Override
        protected QueryExpressionWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return super.buildModel();
        }
    }

    public class JoinSpecificationStarter {
        private final TableExpression joinTable;
        private final JoinType joinType;

        public JoinSpecificationStarter(TableExpression joinTable, JoinType joinType) {
            this.joinTable = joinTable;
            this.joinType = joinType;
        }

        public <T> JoinSpecificationFinisher on(BindableColumn<T> joinColumn, JoinCondition<T> joinCondition) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinType);
        }

        public <T> JoinSpecificationFinisher on(BindableColumn<T> joinColumn, JoinCondition<T> onJoinCondition,
                JoinCriterion<?>... andJoinCriteria) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, onJoinCondition, joinType, andJoinCriteria);
        }
    }

    public class JoinSpecificationFinisher
            extends AbstractWhereStarter<QueryExpressionWhereBuilder, JoinSpecificationFinisher>
            implements Buildable<R> {
        private final JoinSpecification.Builder joinSpecificationBuilder;

        public <T> JoinSpecificationFinisher(TableExpression table, BindableColumn<T> joinColumn,
                JoinCondition<T> joinCondition, JoinType joinType) {
            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<T>()
                    .withConnector("on") //$NON-NLS-1$
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .build();

            joinSpecificationBuilder = JoinSpecification.withJoinTable(table)
                    .withJoinType(joinType)
                    .withJoinCriterion(joinCriterion);

            addJoinSpecificationBuilder(joinSpecificationBuilder);
        }

        public <T> JoinSpecificationFinisher(TableExpression table, BindableColumn<T> joinColumn,
                JoinCondition<T> joinCondition, JoinType joinType, JoinCriterion<?>... andJoinCriteria) {
            JoinCriterion<T> onJoinCriterion = new JoinCriterion.Builder<T>()
                    .withConnector("on") //$NON-NLS-1$
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .build();

            joinSpecificationBuilder = JoinSpecification.withJoinTable(table)
                    .withJoinType(joinType)
                    .withJoinCriterion(onJoinCriterion)
                    .withJoinCriteria(Arrays.asList(andJoinCriteria));

            addJoinSpecificationBuilder(joinSpecificationBuilder);
        }

        @NotNull
        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }

        @Override
        public JoinSpecificationFinisher configureStatement(Consumer<StatementConfiguration> consumer) {
            selectDSL.configureStatement(consumer);
            return this;
        }

        @Override
        public QueryExpressionWhereBuilder where() {
            return QueryExpressionDSL.this.where();
        }

        public <T> JoinSpecificationFinisher and(BindableColumn<T> joinColumn, JoinCondition<T> joinCondition) {
            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<T>()
                    .withConnector("and") //$NON-NLS-1$
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .build();
            joinSpecificationBuilder.withJoinCriterion(joinCriterion);
            return this;
        }

        public JoinSpecificationStarter join(SqlTable joinTable) {
            return QueryExpressionDSL.this.join(joinTable);
        }

        public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
            return QueryExpressionDSL.this.join(joinTable, tableAlias);
        }

        public JoinSpecificationStarter join(Buildable<SelectModel> joinTable, String tableAlias) {
            return QueryExpressionDSL.this.join(joinTable, tableAlias);
        }

        public JoinSpecificationStarter leftJoin(SqlTable joinTable) {
            return QueryExpressionDSL.this.leftJoin(joinTable);
        }

        public JoinSpecificationStarter leftJoin(SqlTable joinTable, String tableAlias) {
            return QueryExpressionDSL.this.leftJoin(joinTable, tableAlias);
        }

        public JoinSpecificationStarter leftJoin(Buildable<SelectModel> joinTable, String tableAlias) {
            return QueryExpressionDSL.this.leftJoin(joinTable, tableAlias);
        }

        public JoinSpecificationStarter rightJoin(SqlTable joinTable) {
            return QueryExpressionDSL.this.rightJoin(joinTable);
        }

        public JoinSpecificationStarter rightJoin(SqlTable joinTable, String tableAlias) {
            return QueryExpressionDSL.this.rightJoin(joinTable, tableAlias);
        }

        public JoinSpecificationStarter rightJoin(Buildable<SelectModel> joinTable, String tableAlias) {
            return QueryExpressionDSL.this.rightJoin(joinTable, tableAlias);
        }

        public JoinSpecificationStarter fullJoin(SqlTable joinTable) {
            return QueryExpressionDSL.this.fullJoin(joinTable);
        }

        public JoinSpecificationStarter fullJoin(SqlTable joinTable, String tableAlias) {
            return QueryExpressionDSL.this.fullJoin(joinTable, tableAlias);
        }

        public JoinSpecificationStarter fullJoin(Buildable<SelectModel> joinTable, String tableAlias) {
            return QueryExpressionDSL.this.fullJoin(joinTable, tableAlias);
        }

        public GroupByFinisher groupBy(BasicColumn... columns) {
            return groupBy(Arrays.asList(columns));
        }

        public GroupByFinisher groupBy(Collection<? extends BasicColumn> columns) {
            return QueryExpressionDSL.this.groupBy(columns);
        }

        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        public SelectDSL<R> orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

        public SelectDSL<R>.LimitFinisher limit(long limit) {
            return QueryExpressionDSL.this.limit(limit);
        }

        public SelectDSL<R>.OffsetFirstFinisher offset(long offset) {
            return QueryExpressionDSL.this.offset(offset);
        }

        public SelectDSL<R>.FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            return QueryExpressionDSL.this.fetchFirst(fetchFirstRows);
        }
    }

    public class GroupByFinisher extends AbstractHavingStarter<QueryExpressionHavingBuilder> implements Buildable<R> {
        public SelectDSL<R> orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

        @NotNull
        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }

        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        public SelectDSL<R>.LimitFinisher limit(long limit) {
            return QueryExpressionDSL.this.limit(limit);
        }

        public SelectDSL<R>.OffsetFirstFinisher offset(long offset) {
            return QueryExpressionDSL.this.offset(offset);
        }

        public SelectDSL<R>.FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            return QueryExpressionDSL.this.fetchFirst(fetchFirstRows);
        }

        @Override
        public QueryExpressionHavingBuilder having() {
            return QueryExpressionDSL.this.having();
        }
    }

    public class UnionBuilder {
        protected final String connector;

        public UnionBuilder(String connector) {
            this.connector = connector;
        }

        public FromGatherer<R> select(BasicColumn... selectList) {
            return select(Arrays.asList(selectList));
        }

        public FromGatherer<R> select(List<BasicColumn> selectList) {
            return new FromGatherer.Builder<R>()
                    .withConnector(connector)
                    .withSelectList(selectList)
                    .withSelectDSL(selectDSL)
                    .build();
        }

        public FromGatherer<R> selectDistinct(BasicColumn... selectList) {
            return selectDistinct(Arrays.asList(selectList));
        }

        public FromGatherer<R> selectDistinct(List<BasicColumn> selectList) {
            return new FromGatherer.Builder<R>()
                    .withConnector(connector)
                    .withSelectList(selectList)
                    .withSelectDSL(selectDSL)
                    .isDistinct()
                    .build();
        }
    }

    public class QueryExpressionHavingBuilder extends AbstractHavingFinisher<QueryExpressionHavingBuilder>
            implements Buildable<R> {

        public SelectDSL<R>.FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            return QueryExpressionDSL.this.fetchFirst(fetchFirstRows);
        }

        public SelectDSL<R>.OffsetFirstFinisher offset(long offset) {
            return QueryExpressionDSL.this.offset(offset);
        }

        public SelectDSL<R>.LimitFinisher limit(long limit) {
            return QueryExpressionDSL.this.limit(limit);
        }

        public SelectDSL<R> orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        @NotNull
        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }

        @Override
        protected QueryExpressionHavingBuilder getThis() {
            return this;
        }

        protected HavingModel buildHavingModel() {
            return super.buildModel();
        }
    }
}
