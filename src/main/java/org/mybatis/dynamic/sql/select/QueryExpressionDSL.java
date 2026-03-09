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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.NullCriterion;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.dsl.AbstractJoinSupport;
import org.mybatis.dynamic.sql.dsl.AbstractQueryingDSL;
import org.mybatis.dynamic.sql.dsl.BooleanOperations;
import org.mybatis.dynamic.sql.dsl.ForAndWaitOperations;
import org.mybatis.dynamic.sql.dsl.GroupByOperations;
import org.mybatis.dynamic.sql.dsl.HavingOperations;
import org.mybatis.dynamic.sql.dsl.JoinOperations;
import org.mybatis.dynamic.sql.dsl.LimitAndOffsetOperations;
import org.mybatis.dynamic.sql.dsl.OrderByOperations;
import org.mybatis.dynamic.sql.dsl.WhereOperations;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;
import org.mybatis.dynamic.sql.where.WhereApplier;

public class QueryExpressionDSL<R> extends AbstractQueryingDSL implements
        JoinOperations<QueryExpressionDSL<R>.JoinSpecificationFinisher>,
        WhereOperations<QueryExpressionDSL<R>.QueryExpressionWhereBuilder>,
        GroupByOperations<QueryExpressionDSL<R>>,
        HavingOperations<QueryExpressionDSL<R>.QueryExpressionHavingBuilder>,
        ConfigurableStatement<QueryExpressionDSL<R>>,
        LimitAndOffsetOperations<SelectDSL<R>, R>,
        ForAndWaitOperations<SelectDSL<R>>,
        OrderByOperations<SelectDSL<R>>,
        Buildable<R> {

    private final @Nullable String connector;
    private final SelectDSL<R> selectDSL;
    private final boolean isDistinct;
    private final List<BasicColumn> selectList;
    private @Nullable QueryExpressionWhereBuilder whereBuilder;
    private @Nullable GroupByModel groupByModel;
    private @Nullable QueryExpressionHavingBuilder havingBuilder;

    protected QueryExpressionDSL(Builder<R> builder) {
        connector = builder.connector;
        selectList = builder.selectList;
        isDistinct = builder.isDistinct;
        selectDSL = Objects.requireNonNull(builder.selectDSL);
        selectDSL.registerQueryExpression(this);
    }

    public QueryExpressionDSL<R> from(Buildable<SelectModel> select) {
        setTable(select);
        return this;
    }

    public QueryExpressionDSL<R> from(Buildable<SelectModel> select, String tableAlias) {
        setTable(select, tableAlias);
        return this;
    }

    public QueryExpressionDSL<R> from(SqlTable table) {
        setTable(table);
        return this;
    }

    public QueryExpressionDSL<R> from(SqlTable table, String tableAlias) {
        setTable(table, tableAlias);
        return this;
    }

    @Override
    public JoinSpecificationFinisher join(JoinType joinType, TableExpression joinTable,
                                          SqlCriterion initialCriterion) {
        var finisher = new JoinSpecificationFinisher(joinType, joinTable, initialCriterion);
        addJoinSpecification(finisher);
        return finisher;
    }

    @Override
    public JoinSpecificationFinisher join(JoinType joinType, SqlTable joinTable, String tableAlias,
                                          SqlCriterion initialCriterion) {
        addTableAlias(joinTable, tableAlias);
        return join(joinType, joinTable, initialCriterion);
    }


    @Override
    public QueryExpressionWhereBuilder where() {
        if (whereBuilder == null) {
            whereBuilder = new QueryExpressionWhereBuilder(new NullCriterion());
        }
        return whereBuilder;
    }

    @Override
    public QueryExpressionWhereBuilder where(SqlCriterion initialCriterion) {
        Validator.assertNull(whereBuilder, Validator.ERROR_32);
        whereBuilder = new QueryExpressionWhereBuilder(initialCriterion);
        return whereBuilder;
    }

    @Override
    public QueryExpressionWhereBuilder applyWhere(WhereApplier whereApplier) {
        Validator.assertNull(whereBuilder, Validator.ERROR_32);
        whereBuilder = new QueryExpressionWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria());
        return whereBuilder;
    }

    @Override
    public QueryExpressionDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        selectDSL.configureStatement(consumer);
        return this;
    }

    @Override
    public QueryExpressionHavingBuilder having(SqlCriterion initialCriterion) {
        Validator.assertNull(havingBuilder, "ERROR.31"); //$NON-NLS-1$
        havingBuilder = new QueryExpressionHavingBuilder(initialCriterion);
        return havingBuilder;
    }

    @Override
    public QueryExpressionHavingBuilder applyHaving(HavingApplier havingApplier) {
        Validator.assertNull(havingBuilder, "ERROR.31"); //$NON-NLS-1$
        havingBuilder = new QueryExpressionHavingBuilder(havingApplier.initialCriterion(), havingApplier.subCriteria());
        return havingBuilder;
    }

    @Override
    public R build() {
        return selectDSL.build();
    }

    public QueryExpressionDSL<R> groupBy(Collection<? extends BasicColumn> columns) {
        groupByModel = GroupByModel.of(columns);
        return this;
    }

    @Override
    public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
        return selectDSL.orderBy(columns);
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
                .withJoinModel(buildJoinModel())
                .withGroupByModel(groupByModel)
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .withHavingModel(havingBuilder == null ? null : havingBuilder.buildHavingModel())
                .build();
    }

    @Override
    public SelectDSL<R> setWaitClause(String waitClause) {
        return selectDSL.setWaitClause(waitClause);
    }

    @Override
    public SelectDSL<R> setForClause(String forClause) {
        return selectDSL.setForClause(forClause);
    }

    @Override
    public LimitFinisher<SelectDSL<R>, R> limitWhenPresent(@Nullable Long limit) {
        return selectDSL.limitWhenPresent(limit);
    }

    @Override
    public OffsetFirstFinisher<SelectDSL<R>, R> offsetWhenPresent(@Nullable Long offset) {
        return selectDSL.offsetWhenPresent(offset);
    }

    @Override
    public FetchFirstFinisher<SelectDSL<R>> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        return selectDSL.fetchFirstWhenPresent(fetchFirstRows);
    }

    public class QueryExpressionWhereBuilder
            implements BooleanOperations<QueryExpressionWhereBuilder>,
            ConfigurableStatement<QueryExpressionWhereBuilder>,
            OrderByOperations<SelectDSL<R>>,
            GroupByOperations<QueryExpressionDSL<R>>,
            ForAndWaitOperations<SelectDSL<R>>,
            LimitAndOffsetOperations<SelectDSL<R>, R>,
            Buildable<R> {
        protected final SqlCriterion initialCriterion;
        protected final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

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
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        @Override
        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

        public QueryExpressionDSL<R> groupBy(Collection<? extends BasicColumn> columns) {
            return QueryExpressionDSL.this.groupBy(columns);
        }

        @Override
        public QueryExpressionWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
            QueryExpressionDSL.this.configureStatement(consumer);
            return this;
        }

        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }

        @Override
        public SelectDSL<R> setWaitClause(String waitClause) {
            return QueryExpressionDSL.this.setWaitClause(waitClause);
        }

        @Override
        public SelectDSL<R> setForClause(String forClause) {
            return QueryExpressionDSL.this.setForClause(forClause);
        }

        @Override
        public LimitFinisher<SelectDSL<R>, R> limitWhenPresent(@Nullable Long limit) {
            return QueryExpressionDSL.this.limitWhenPresent(limit);
        }

        @Override
        public OffsetFirstFinisher<SelectDSL<R>, R> offsetWhenPresent(@Nullable Long offset) {
            return QueryExpressionDSL.this.offsetWhenPresent(offset);
        }

        @Override
        public FetchFirstFinisher<SelectDSL<R>> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            return QueryExpressionDSL.this.fetchFirstWhenPresent(fetchFirstRows);
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return new EmbeddedWhereModel.Builder()
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }
    }

    public class JoinSpecificationFinisher
            extends AbstractJoinSupport<QueryExpressionDSL<R>, JoinSpecificationFinisher>
            implements WhereOperations<QueryExpressionWhereBuilder>,
            ConfigurableStatement<JoinSpecificationFinisher>,
            GroupByOperations<QueryExpressionDSL<R>>,
            ForAndWaitOperations<SelectDSL<R>>,
            LimitAndOffsetOperations<SelectDSL<R>, R>,
            OrderByOperations<SelectDSL<R>>,
            Buildable<R> {
        protected JoinSpecificationFinisher(JoinType joinType, TableExpression joinTable,
                                            SqlCriterion initialCriterion) {
            super(joinType, joinTable, initialCriterion);
        }

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

        @Override
        public QueryExpressionWhereBuilder where(SqlCriterion initialCriterion) {
            return QueryExpressionDSL.this.where(initialCriterion);
        }

        @Override
        public QueryExpressionWhereBuilder applyWhere(WhereApplier whereApplier) {
            return QueryExpressionDSL.this.applyWhere(whereApplier);
        }

        public QueryExpressionDSL<R> groupBy(Collection<? extends BasicColumn> columns) {
            return QueryExpressionDSL.this.groupBy(columns);
        }

        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        @Override
        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

        @Override
        protected JoinSpecificationFinisher getThis() {
            return this;
        }

        @Override
        public JoinSpecificationFinisher join(JoinType joinType, TableExpression joinTable,
                                              SqlCriterion initialCriterion) {
            return QueryExpressionDSL.this.join(joinType, joinTable, initialCriterion);
        }

        @Override
        public JoinSpecificationFinisher join(JoinType joinType, SqlTable joinTable, String tableAlias,
                                              SqlCriterion initialCriterion) {
            return QueryExpressionDSL.this.join(joinType, joinTable, tableAlias, initialCriterion);
        }

        @Override
        public QueryExpressionDSL<R> endJoin() {
            return QueryExpressionDSL.this;
        }

        @Override
        public LimitFinisher<SelectDSL<R>, R> limitWhenPresent(@Nullable Long limit) {
            return QueryExpressionDSL.this.limitWhenPresent(limit);
        }

        @Override
        public OffsetFirstFinisher<SelectDSL<R>, R> offsetWhenPresent(@Nullable Long offset) {
            return QueryExpressionDSL.this.offsetWhenPresent(offset);
        }

        @Override
        public FetchFirstFinisher<SelectDSL<R>> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            return QueryExpressionDSL.this.fetchFirstWhenPresent(fetchFirstRows);
        }

        @Override
        public SelectDSL<R> setWaitClause(String waitClause) {
            return QueryExpressionDSL.this.setWaitClause(waitClause);
        }

        @Override
        public SelectDSL<R> setForClause(String forClause) {
            return QueryExpressionDSL.this.setForClause(forClause);
        }
    }

    public class UnionBuilder {
        protected final String connector;

        public UnionBuilder(String connector) {
            this.connector = connector;
        }

        public QueryExpressionDSL<R> select(BasicColumn... selectList) {
            return select(Arrays.asList(selectList));
        }

        public QueryExpressionDSL<R> select(List<BasicColumn> selectList) {
            return new Builder<R>()
                    .withConnector(connector)
                    .withSelectList(selectList)
                    .withSelectDSL(selectDSL)
                    .build();
        }

        public QueryExpressionDSL<R> selectDistinct(BasicColumn... selectList) {
            return selectDistinct(Arrays.asList(selectList));
        }

        public QueryExpressionDSL<R> selectDistinct(List<BasicColumn> selectList) {
            return new Builder<R>()
                    .withConnector(connector)
                    .withSelectList(selectList)
                    .withSelectDSL(selectDSL)
                    .isDistinct()
                    .build();
        }
    }

    public class QueryExpressionHavingBuilder implements BooleanOperations<QueryExpressionHavingBuilder>,
            ForAndWaitOperations<SelectDSL<R>>,
            LimitAndOffsetOperations<SelectDSL<R>, R>,
            OrderByOperations<SelectDSL<R>>,
            Buildable<R> {
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
        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }

        protected HavingModel buildHavingModel() {
            return new HavingModel.Builder()
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }

        @Override
        public SelectDSL<R> setWaitClause(String waitClause) {
            return QueryExpressionDSL.this.setWaitClause(waitClause);
        }

        @Override
        public SelectDSL<R> setForClause(String forClause) {
            return QueryExpressionDSL.this.setForClause(forClause);
        }

        @Override
        public LimitFinisher<SelectDSL<R>, R> limitWhenPresent(@Nullable Long limit) {
            return QueryExpressionDSL.this.limitWhenPresent(limit);
        }

        @Override
        public OffsetFirstFinisher<SelectDSL<R>, R> offsetWhenPresent(@Nullable Long offset) {
            return QueryExpressionDSL.this.offsetWhenPresent(offset);
        }

        @Override
        public FetchFirstFinisher<SelectDSL<R>> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            return QueryExpressionDSL.this.fetchFirstWhenPresent(fetchFirstRows);
        }
    }

    public static class Builder<R> {
        private @Nullable String connector;
        private final List<BasicColumn> selectList = new ArrayList<>();
        private @Nullable SelectDSL<R> selectDSL;
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

        public QueryExpressionDSL<R> build() {
            return new QueryExpressionDSL<>(this);
        }
    }
}
