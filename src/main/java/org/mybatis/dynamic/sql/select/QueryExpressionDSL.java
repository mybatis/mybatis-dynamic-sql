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
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.dsl.AbstractBooleanOperationsFinisher;
import org.mybatis.dynamic.sql.dsl.AbstractJoinSpecificationFinisher;
import org.mybatis.dynamic.sql.dsl.AbstractQueryingDSL;
import org.mybatis.dynamic.sql.dsl.HavingOperations;
import org.mybatis.dynamic.sql.dsl.JoinOperations;
import org.mybatis.dynamic.sql.dsl.WhereOperations;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class QueryExpressionDSL<R> extends AbstractQueryingDSL implements
        JoinOperations<QueryExpressionDSL<R>, QueryExpressionDSL<R>.JoinSpecificationFinisher>,
        WhereOperations<QueryExpressionDSL<R>.QueryExpressionWhereBuilder>,
        HavingOperations<QueryExpressionDSL<R>.QueryExpressionHavingBuilder>,
        ConfigurableStatement<QueryExpressionDSL<R>>, SelectDSLOperations<R>, Buildable<R> {
    private static final String ERROR_27 = "ERROR.27"; //$NON-NLS-1$

    private final @Nullable String connector;
    private final SelectDSL<R> selectDSL;
    private final boolean isDistinct;
    private final List<BasicColumn> selectList;
    private @Nullable TableExpression table;
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
        Validator.assertNull(table, ERROR_27);
        table = buildSubQuery(select);
        return this;
    }

    public QueryExpressionDSL<R> from(Buildable<SelectModel> select, String tableAlias) {
        Validator.assertNull(table, ERROR_27);
        table = buildSubQuery(select, tableAlias);
        return this;
    }

    public QueryExpressionDSL<R> from(SqlTable table) {
        Validator.assertNull(this.table, ERROR_27);
        this.table = table;
        return this;
    }

    public QueryExpressionDSL<R> from(SqlTable table, String tableAlias) {
        Validator.assertNull(this.table, ERROR_27);
        this.table = table;
        addTableAlias(table, tableAlias);
        return this;
    }

    @Override
    public JoinSpecificationFinisher buildJoinFinisher() {
        var finisher = new JoinSpecificationFinisher();
        joinSpecifications.add(finisher);
        return finisher;
    }

    @Override
    public QueryExpressionWhereBuilder where() {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder, QueryExpressionWhereBuilder::new);
        return whereBuilder;
    }

    @Override
    public QueryExpressionDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        selectDSL.configureStatement(consumer);
        return this;
    }

    @Override
    public QueryExpressionHavingBuilder having() {
        havingBuilder = Objects.requireNonNullElseGet(havingBuilder, QueryExpressionHavingBuilder::new);
        return havingBuilder;
    }

    @Override
    public R build() {
        return selectDSL.build();
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
        Validator.assertTrue(table != null, ERROR_27);
        return QueryExpressionModel.withSelectList(selectList)
                .withConnector(connector)
                .withTable(table)
                .isDistinct(isDistinct)
                .withTableAliases(tableAliases)
                .withJoinModel(buildJoinModel())
                .withGroupByModel(groupByModel)
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .withHavingModel(havingBuilder == null ? null : havingBuilder.buildHavingModel())
                .build();
    }

    @Override
    public SelectDSL<R> getSelectDSL() {
        return selectDSL;
    }

    public class QueryExpressionWhereBuilder extends AbstractBooleanOperationsFinisher<QueryExpressionWhereBuilder>
            implements ConfigurableStatement<QueryExpressionWhereBuilder>, Buildable<R>, SelectDSLOperations<R> {
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
        protected QueryExpressionWhereBuilder getThis() {
            return this;
        }

        @Override
        public SelectDSL<R> getSelectDSL() {
            return QueryExpressionDSL.this.getSelectDSL();
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return toWhereModel();
        }
    }

    public class JoinSpecificationFinisher
            extends AbstractJoinSpecificationFinisher<QueryExpressionDSL<R>, JoinSpecificationFinisher>
            implements JoinOperations<QueryExpressionDSL<R>, JoinSpecificationFinisher>,
            WhereOperations<QueryExpressionWhereBuilder>,
            ConfigurableStatement<JoinSpecificationFinisher>, Buildable<R>,
            SelectDSLOperations<R> {

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

        @Override
        protected JoinSpecificationFinisher getThis() {
            return this;
        }

        @Override
        public QueryExpressionDSL<R> endJoinSpecification() {
            return QueryExpressionDSL.this;
        }

        @Override
        public SelectDSL<R> getSelectDSL() {
            return QueryExpressionDSL.this.getSelectDSL();
        }

        @Override
        public void addTableAlias(SqlTable table, String tableAlias) {
            QueryExpressionDSL.this.addTableAlias(table, tableAlias);
        }

        @Override
        public JoinSpecificationFinisher buildJoinFinisher() {
            return QueryExpressionDSL.this.buildJoinFinisher();
        }
    }

    public class GroupByFinisher implements HavingOperations<QueryExpressionHavingBuilder>,
            Buildable<R>, SelectDSLOperations<R> {
        public SelectDSL<R> orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }

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

        @Override
        public QueryExpressionHavingBuilder having() {
            return QueryExpressionDSL.this.having();
        }

        @Override
        public SelectDSL<R> getSelectDSL() {
            return QueryExpressionDSL.this.getSelectDSL();
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

    public class QueryExpressionHavingBuilder extends AbstractBooleanOperationsFinisher<QueryExpressionHavingBuilder>
            implements Buildable<R>, SelectDSLOperations<R> {

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

        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }

        @Override
        protected QueryExpressionHavingBuilder getThis() {
            return this;
        }

        protected HavingModel buildHavingModel() {
            return toHavingModel();
        }

        @Override
        public SelectDSL<R> getSelectDSL() {
            return QueryExpressionDSL.this.getSelectDSL();
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
