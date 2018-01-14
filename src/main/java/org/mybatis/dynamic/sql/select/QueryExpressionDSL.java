/**
 *    Copyright 2016-2018 the original author or authors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;
import org.mybatis.dynamic.sql.where.WhereModel;

public class QueryExpressionDSL<R> implements Buildable<R> {

    private String connector;
    private SelectDSL<R> selectDSL;
    private boolean isDistinct;
    private List<BasicColumn> selectList;
    private SqlTable table;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    private WhereModel whereModel;
    private GroupByModel groupByModel;
    private JoinModel joinModel;
    private List<JoinSpecification> joinSpecifications = new ArrayList<>();
    
    private QueryExpressionDSL(FromGatherer<R> fromGatherer) {
        connector = fromGatherer.builder.connector;
        selectList = Arrays.asList(fromGatherer.builder.selectList);
        isDistinct = fromGatherer.builder.isDistinct;
        selectDSL = Objects.requireNonNull(fromGatherer.builder.selectDSL);
        table = Objects.requireNonNull(fromGatherer.table);
        tableAliases.putAll(fromGatherer.tableAliasMap);
    }
    
    public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new QueryExpressionWhereBuilder(column, condition);
    }

    public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return new QueryExpressionWhereBuilder(column, condition, subCriteria);
    }
    
    @Override
    public R build() {
        selectDSL.addQueryExpression(buildModel());
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
        selectDSL.addQueryExpression(buildModel());
        return new GroupByFinisher();
    }
    
    public SelectDSL<R> orderBy(SortSpecification...columns) {
        selectDSL.addQueryExpression(buildModel());
        selectDSL.setOrderByModel(OrderByModel.of(columns));
        return selectDSL;
    }

    public UnionBuilder union() {
        selectDSL.addQueryExpression(buildModel());
        return new UnionBuilder();
    }

    protected QueryExpressionModel buildModel() {
        return QueryExpressionModel.withSelectList(selectList)
                .withConnector(connector)
                .withTable(table)
                .isDistinct(isDistinct)
                .withTableAliases(tableAliases)
                .withWhereModel(whereModel)
                .withJoinModel(joinModel)
                .withGroupByModel(groupByModel)
                .build();
    }
    
    public static class FromGatherer<R> {
        private FromGathererBuilder<R> builder;
        private Map<SqlTable, String> tableAliasMap = new HashMap<>();
        private SqlTable table;
        
        public FromGatherer(FromGathererBuilder<R> builder) {
            this.builder = builder;
        }
        
        public QueryExpressionDSL<R> from(SqlTable table) {
            this.table = table;
            
            return new QueryExpressionDSL<>(this);
        }

        public QueryExpressionDSL<R> from(SqlTable table, String tableAlias) {
            this.table = table;
            tableAliasMap.put(table, tableAlias);
            return new QueryExpressionDSL<>(this);
        }
    }
    
    public static class FromGathererBuilder<R> {
        private String connector;
        private BasicColumn[] selectList;
        private SelectDSL<R> selectDSL;
        private boolean isDistinct;
        
        public FromGathererBuilder<R> withConnector(String connector) {
            this.connector = connector;
            return this;
        }

        public FromGathererBuilder<R> withSelectList(BasicColumn[] selectList) {
            this.selectList = selectList;
            return this;
        }

        public FromGathererBuilder<R> withSelectDSL(SelectDSL<R> selectDSL) {
            this.selectDSL = selectDSL;
            return this;
        }
        
        public FromGathererBuilder<R> isDistinct() {
            this.isDistinct = true;
            return this;
        }
        
        public FromGatherer<R> build() {
            return new FromGatherer<>(this);
        }
    }
    
    public class QueryExpressionWhereBuilder extends AbstractWhereDSL<QueryExpressionWhereBuilder>
            implements Buildable<R> {
        private <T> QueryExpressionWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        private <T> QueryExpressionWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public UnionBuilder union() {
            whereModel = buildWhereModel();
            selectDSL.addQueryExpression(buildModel());
            return new UnionBuilder();
        }

        public SelectDSL<R> orderBy(SortSpecification...columns) {
            whereModel = buildWhereModel();
            selectDSL.addQueryExpression(buildModel());
            selectDSL.setOrderByModel(OrderByModel.of(columns));
            return selectDSL;
        }
        
        public GroupByFinisher groupBy(BasicColumn...columns) {
            groupByModel = GroupByModel.of(columns);
            whereModel = buildWhereModel();
            selectDSL.addQueryExpression(buildModel());
            return new GroupByFinisher();
        }
        
        @Override
        public R build() {
            whereModel = buildWhereModel();
            selectDSL.addQueryExpression(buildModel());
            return selectDSL.build();
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

        public JoinSpecificationFinisher on(BasicColumn joinColumn, JoinCondition joinCondition,
                JoinCriterion...joinCriteria) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinType, joinCriteria);
        }
    }

    public class JoinSpecificationFinisher implements Buildable<R> {

        private SqlTable joinTable;
        private List<JoinCriterion> joinCriteria = new ArrayList<>();
        private JoinType joinType;
        
        public JoinSpecificationFinisher(SqlTable table, BasicColumn joinColumn,
                JoinCondition joinCondition, JoinType joinType) {
            this.joinTable = table;
            this.joinType = joinType;

            JoinCriterion joinCriterion = JoinCriterion.withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .withConnector("on") //$NON-NLS-1$
                    .build();
            
            joinCriteria.add(joinCriterion);
        }

        public JoinSpecificationFinisher(SqlTable table, BasicColumn joinColumn,
                JoinCondition joinCondition, JoinType joinType, JoinCriterion...joinCriteria) {
            this.joinTable = table;
            this.joinType = joinType;

            JoinCriterion joinCriterion = JoinCriterion.withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .withConnector("on") //$NON-NLS-1$
                    .build();
            
            this.joinCriteria.add(joinCriterion);
            this.joinCriteria.addAll(Arrays.asList(joinCriteria));
        }
        
        protected JoinSpecification buildJoinSpecification() {
            return JoinSpecification.withJoinTable(joinTable)
                    .withJoinCriteria(joinCriteria)
                    .withJoinType(joinType)
                    .build();
        }
        
        protected JoinModel buildJoinModel() {
            joinSpecifications.add(buildJoinSpecification());
            return JoinModel.of(joinSpecifications);
        }
        
        @Override
        public R build() {
            joinModel = buildJoinModel();
            selectDSL.addQueryExpression(buildModel());
            return selectDSL.build();
        }
        
        public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
            joinModel = buildJoinModel();
            return new QueryExpressionWhereBuilder(column, condition);
        }

        public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            joinModel = buildJoinModel();
            return new QueryExpressionWhereBuilder(column, condition, subCriteria);
        }

        public JoinSpecificationFinisher and(BasicColumn joinColumn, JoinCondition joinCondition) {
            JoinCriterion joinCriterion = JoinCriterion.withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .withConnector("and") //$NON-NLS-1$
                    .build();
            this.joinCriteria.add(joinCriterion);
            return this;
        }

        public JoinSpecificationStarter join(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.INNER);
        }
        
        public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return join(joinTable);
        }

        public JoinSpecificationStarter leftJoin(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.LEFT);
        }
        
        public JoinSpecificationStarter leftJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return leftJoin(joinTable);
        }

        public JoinSpecificationStarter rightJoin(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.RIGHT);
        }
        
        public JoinSpecificationStarter rightJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return rightJoin(joinTable);
        }

        public JoinSpecificationStarter fullJoin(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.FULL);
        }
        
        public JoinSpecificationStarter fullJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return fullJoin(joinTable);
        }

        public SelectDSL<R> orderBy(SortSpecification...columns) {
            joinModel = buildJoinModel();
            selectDSL.addQueryExpression(buildModel());
            selectDSL.setOrderByModel(OrderByModel.of(columns));
            return selectDSL;
        }
    }
    
    public class GroupByFinisher implements Buildable<R> {
        public SelectDSL<R> orderBy(SortSpecification...columns) {
            selectDSL.setOrderByModel(OrderByModel.of(columns));
            return selectDSL;
        }

        @Override
        public R build() {
            return selectDSL.build();
        }
    }
    
    public class UnionBuilder {
        public FromGatherer<R> select(BasicColumn...selectList) {
            return new FromGathererBuilder<R>()
                    .withConnector("union") //$NON-NLS-1$
                    .withSelectList(selectList)
                    .withSelectDSL(selectDSL)
                    .build();
        }

        public FromGatherer<R> selectDistinct(BasicColumn...selectList) {
            return new FromGathererBuilder<R>()
                    .withConnector("union") //$NON-NLS-1$
                    .isDistinct()
                    .withSelectList(selectList)
                    .withSelectDSL(selectDSL)
                    .build();
        }
    }
}
