/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

public class QueryExpressionDSL<R> extends AbstractQueryExpressionDSL<QueryExpressionDSL<R>, R>
        implements Buildable<R> {

    private String connector;
    private SelectDSL<R> selectDSL;
    private boolean isDistinct;
    private List<BasicColumn> selectList;
    private QueryExpressionWhereBuilder whereBuilder = new QueryExpressionWhereBuilder();
    private GroupByModel groupByModel;
    
    QueryExpressionDSL(FromGatherer<R> fromGatherer) {
        super(fromGatherer.table);
        connector = fromGatherer.connector;
        selectList = fromGatherer.selectList;
        isDistinct = fromGatherer.isDistinct;
        selectDSL = Objects.requireNonNull(fromGatherer.selectDSL);
    }
    
    QueryExpressionDSL(FromGatherer<R> fromGatherer, String tableAlias) {
        this(fromGatherer);
        tableAliases.put(fromGatherer.table, tableAlias);
    }
    
    public QueryExpressionWhereBuilder where() {
        return whereBuilder;
    }

    public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        whereBuilder.and(column, condition, subCriteria);
        return whereBuilder;
    }

    @Override
    public R build() {
        return selectDSL.build();
    }

    public JoinSpecificationStarter join(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.INNER);
    }
    
    public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
        tableAliases.put(joinTable, tableAlias);
        return join(joinTable);
    }

    public JoinSpecificationStarter leftJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.LEFT);
    }
    
    public JoinSpecificationStarter leftJoin(SqlTable joinTable, String tableAlias) {
        tableAliases.put(joinTable, tableAlias);
        return leftJoin(joinTable);
    }

    public JoinSpecificationStarter rightJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.RIGHT);
    }
    
    public JoinSpecificationStarter rightJoin(SqlTable joinTable, String tableAlias) {
        tableAliases.put(joinTable, tableAlias);
        return rightJoin(joinTable);
    }

    public JoinSpecificationStarter fullJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter(joinTable, JoinType.FULL);
    }
    
    public JoinSpecificationStarter fullJoin(SqlTable joinTable, String tableAlias) {
        tableAliases.put(joinTable, tableAlias);
        return fullJoin(joinTable);
    }

    public GroupByFinisher groupBy(BasicColumn...columns) {
        groupByModel = GroupByModel.of(columns);
        return new GroupByFinisher();
    }
    
    public SelectDSL<R> orderBy(SortSpecification...columns) {
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
                .withTableAliases(tableAliases)
                .withWhereModel(whereBuilder.buildWhereModel())
                .withJoinModel(buildJoinModel().orElse(null))
                .withGroupByModel(groupByModel)
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
        private String connector;
        private List<BasicColumn> selectList;
        private SelectDSL<R> selectDSL;
        private boolean isDistinct;
        private SqlTable table;
        
        public FromGatherer(Builder<R> builder) {
            this.connector = builder.connector;
            this.selectList = Objects.requireNonNull(builder.selectList);
            this.selectDSL = Objects.requireNonNull(builder.selectDSL);
            this.isDistinct = builder.isDistinct;
        }
        
        public QueryExpressionDSL<R> from(SqlTable table) {
            this.table = table;
            return selectDSL.newQueryExpression(this);
        }

        public QueryExpressionDSL<R> from(SqlTable table, String tableAlias) {
            this.table = table;
            return selectDSL.newQueryExpression(this, tableAlias);
        }
        
        public static class Builder<R> {
            private String connector;
            private List<BasicColumn> selectList = new ArrayList<>();
            private SelectDSL<R> selectDSL;
            private boolean isDistinct;
            
            public Builder<R> withConnector(String connector) {
                this.connector = connector;
                return this;
            }

            public Builder<R> withSelectList(Collection<BasicColumn> selectList) {
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
    
    public class QueryExpressionWhereBuilder extends AbstractWhereDSL<QueryExpressionWhereBuilder>
            implements Buildable<R> {
        private <T> QueryExpressionWhereBuilder() {
        }

        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        public SelectDSL<R> orderBy(SortSpecification...columns) {
            return QueryExpressionDSL.this.orderBy(columns);
        }
        
        public GroupByFinisher groupBy(BasicColumn...columns) {
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
        
        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }
        
        @Override
        protected QueryExpressionWhereBuilder getThis() {
            return this;
        }
    }
    
    public class JoinSpecificationStarter {
        private SqlTable joinTable;
        private JoinType joinType;
        
        public JoinSpecificationStarter(SqlTable joinTable, JoinType joinType) {
            this.joinTable = joinTable;
            this.joinType = joinType;
        }

        public JoinSpecificationFinisher on(BasicColumn joinColumn, JoinCondition joinCondition) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinType);
        }

        public JoinSpecificationFinisher on(BasicColumn joinColumn, JoinCondition onJoinCondition,
                JoinCriterion...andJoinCriteria) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, onJoinCondition, joinType, andJoinCriteria);
        }
    }

    public class JoinSpecificationFinisher implements Buildable<R> {
        private JoinSpecification.Builder joinSpecificationBuilder;
        
        public JoinSpecificationFinisher(SqlTable table, BasicColumn joinColumn,
                JoinCondition joinCondition, JoinType joinType) {
            JoinCriterion joinCriterion = new JoinCriterion.Builder()
                    .withConnector("on") //$NON-NLS-1$
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .build();

            joinSpecificationBuilder = JoinSpecification.withJoinTable(table)
                    .withJoinType(joinType)
                    .withJoinCriterion(joinCriterion);

            addJoinSpecificationBuilder(joinSpecificationBuilder);
        }

        public JoinSpecificationFinisher(SqlTable table, BasicColumn joinColumn,
                JoinCondition joinCondition, JoinType joinType, JoinCriterion...andJoinCriteria) {
            JoinCriterion onJoinCriterion = new JoinCriterion.Builder()
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
        
        @Override
        public R build() {
            return QueryExpressionDSL.this.build();
        }
        
        public QueryExpressionWhereBuilder where() {
            return QueryExpressionDSL.this.where();
        }
        
        public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            return QueryExpressionDSL.this.where(column, condition, subCriteria);
        }

        public JoinSpecificationFinisher and(BasicColumn joinColumn, JoinCondition joinCondition) {
            JoinCriterion joinCriterion = new JoinCriterion.Builder()
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

        public JoinSpecificationStarter leftJoin(SqlTable joinTable) {
            return QueryExpressionDSL.this.leftJoin(joinTable);
        }
        
        public JoinSpecificationStarter leftJoin(SqlTable joinTable, String tableAlias) {
            return QueryExpressionDSL.this.leftJoin(joinTable, tableAlias);
        }

        public JoinSpecificationStarter rightJoin(SqlTable joinTable) {
            return QueryExpressionDSL.this.rightJoin(joinTable);
        }
        
        public JoinSpecificationStarter rightJoin(SqlTable joinTable, String tableAlias) {
            return QueryExpressionDSL.this.rightJoin(joinTable, tableAlias);
        }

        public JoinSpecificationStarter fullJoin(SqlTable joinTable) {
            return QueryExpressionDSL.this.fullJoin(joinTable);
        }
        
        public JoinSpecificationStarter fullJoin(SqlTable joinTable, String tableAlias) {
            return QueryExpressionDSL.this.fullJoin(joinTable, tableAlias);
        }

        public GroupByFinisher groupBy(BasicColumn...columns) {
            return QueryExpressionDSL.this.groupBy(columns);
        }
        
        public UnionBuilder union() {
            return QueryExpressionDSL.this.union();
        }

        public UnionBuilder unionAll() {
            return QueryExpressionDSL.this.unionAll();
        }

        public SelectDSL<R> orderBy(SortSpecification...columns) {
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
    
    public class GroupByFinisher implements Buildable<R> {
        public SelectDSL<R> orderBy(SortSpecification...columns) {
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
    
    public class UnionBuilder {
        protected String connector;
        
        public UnionBuilder(String connector) {
            this.connector = connector;
        }
        
        public FromGatherer<R> select(BasicColumn...selectList) {
            return select(Arrays.asList(selectList));
        }

        public FromGatherer<R> select(List<BasicColumn> selectList) {
            return new FromGatherer.Builder<R>()
                    .withConnector(connector)
                    .withSelectList(selectList)
                    .withSelectDSL(selectDSL)
                    .build();
        }

        public FromGatherer<R> selectDistinct(BasicColumn...selectList) {
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
}
